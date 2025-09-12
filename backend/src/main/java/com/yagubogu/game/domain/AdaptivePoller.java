package com.yagubogu.game.domain;

import static com.yagubogu.game.domain.GameState.FINALIZED_GAME_STATES;

import com.yagubogu.game.dto.KboGameResponse;
import com.yagubogu.game.dto.KboGamesResponse;
import com.yagubogu.game.exception.GameSyncException;
import com.yagubogu.game.repository.GameRepository;
import com.yagubogu.game.service.GameResultSyncService;
import com.yagubogu.game.service.client.KboGameSyncClient;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdaptivePoller {

    // MAX_FAIL_COUNT: 경기별 실패 횟수 카운트 상한 (이후에도 카운트되지만 경고 기준)
    // DEFAULT_START_TIME: KBO 시작 시간이 null일 때 사용할 기본 킥오프 시간
    // MAX_FAIL_MINUTE: 경기별/전역 지수 백오프의 최대 상한(분)
    // (요구사항: LIVE=8분, SCHEDULED=20분, 전역=5~8분 → 여기선 전역/경기별 공통 cap=8분)

    private static final int MAX_FAIL_COUNT = 3;
    private static final LocalTime DEFAULT_START_TIME = LocalTime.of(18, 30);
    private static final int MAX_FAIL_MINUTE = 8;

    private final GameRepository gameRepository;
    private final KboGameSyncClient kboGameSyncClient;
    private final GameResultSyncService gameResultSyncService;
    private final Clock clock;

    // 경기별 실패 횟수 (지수 백오프 계산용)
    private final Map<Long, Integer> failureCount = new ConcurrentHashMap<>();

    // 전역 fetch 실패 시, 이 시각까지는 tick을 중단
    private volatile Instant globalBackoffUntil = Instant.EPOCH;

    // 오늘 더 이상 처리할 due가 없을 때, 다음 전체 깨울 시각(대개 자정)
    private volatile Instant globalWakeAt = Instant.EPOCH;

    // 경기별 다음 실행 예정 시각 (due)
    private final Map<Long, Instant> nextRunAt = new ConcurrentHashMap<>();

    // 앱 재시작/자정 이후 초기화: 오늘자 경기만 가지고 nextRunAt를 재구성
    // - FINALIZED 상태 경기는 스케줄 잡지 않음
    // - 각 경기의 next due를 계산 후, 전체적으로 가장 이른 due를 globalWakeAt으로 세팅
    public void initializeTodaySchedule(LocalDate today) {
        List<Game> games = gameRepository.findAllByDate(today);
        nextRunAt.clear();

        if (games.isEmpty()) {
            // 오늘 경기가 없다면 다음 자정까지 전체 슬립
            globalWakeAt = calculateTomorrow(today);
            return;
        }

        Instant earliestDue = null;
        Instant now = Instant.now(clock);

        for (Game g : games) {
            // 이미 끝난 경기는 스케줄 잡지 않음
            if (FINALIZED_GAME_STATES.contains(g.getGameState())) {
                continue;
            }

            // 시작 시각/현재 상태를 기준으로 첫 폴링 due 계산
            Instant planned = computeStartInstant(g.getDate(), g.getStartAt());
            Duration interval = computePollingInterval(g.getDate(), g.getStartAt(), now, g.getGameState());
            Instant due = computeInitialDue(planned, now, interval);

            nextRunAt.put(g.getId(), due);
            earliestDue = earliestOf(earliestDue, due);
        }

        // globalWakeAt = earliestDue(있으면) vs now
        globalWakeAt = Optional.ofNullable(earliestDue)
                .filter(d -> d.isAfter(now))
                .orElse(now);
    }

    // 1분 간격으로 깨어나 폴링을 수행
    // - 전역 백오프/웨이크업 시각 검사
    // - 오늘 처리할 경기 존재 여부 확인
    // - KBO 일괄 조회(오늘자) → 실패 시 전역 백오프, 성공 시 캐시로 사용
    // - 각 경기별 due 도달 판단 후 세부 업데이트 및 다음 due 재설정
    @Scheduled(fixedDelay = 60_000)
    public void pollGameWhenReachDue() {
        Instant now = Instant.now(clock);

        // 전역 백오프 중이면 쉬고 복귀
        if (now.isBefore(globalBackoffUntil)) {
            return;
        }

        // 아직 깰 시간이 아니면 복귀
        if (now.isBefore(globalWakeAt)) {
            return;
        }

        LocalDate today = LocalDate.now(clock);
        // 오늘 남은 경기(미종료)가 없으면 자정까지 슬립
        if (!existsRemainGames(today)) {
            globalWakeAt = calculateTomorrow(today);
            return;
        }

        // 오늘자 경기 목록 1회 일괄 fetch (캐시처럼 활용)
        Map<String, KboGameResponse> dailyGames = fetchGames(today);
        if (dailyGames.isEmpty()) {
            return;
        }

        // 성공 시 전역 백오프 해제
        clearGlobalBackoff();

        // 경기별 처리 루프
        for (Game game : gameRepository.findAllByDate(today)) {
            Long gameId = game.getId();

            // 종료된 경기는 스케줄 및 실패 카운트 제거
            if (FINALIZED_GAME_STATES.contains(game.getGameState())) {
                nextRunAt.remove(gameId);
                failureCount.remove(gameId);
                continue;
            }

            // 아직 due가 안 왔으면 skip
            Instant dueAt = nextRunAt.getOrDefault(gameId, Instant.EPOCH);
            if (now.isBefore(dueAt)) {
                continue;
            }

            try {
                // 오늘자 캐시에서 해당 경기 행 추출(없으면 실패 처리)
                KboGameResponse row = dailyGames.get(game.getGameCode());
                if (row == null) {
                    applyPerGameBackOff(gameId);
                    continue;
                }

                gameResultSyncService.updateGameDetails(game.getGameCode(), row);
                resetGameFailureCount(gameId);

                // 동기화 결과가 종료 상태면 스케줄 제거
                GameState newState = row.gameState();
                if (isFinalState(newState)) {
                    nextRunAt.remove(gameId);
                    failureCount.remove(gameId);
                    continue;
                }

                // 다음 due 산정 (상태/경과 시간 기반)
                Duration base = computePollingInterval(game.getDate(), game.getStartAt(), now, newState);
                scheduleNextRuns(gameId, base);

            } catch (Exception e) {
                // 개별 경기 처리 실패 시: 경기별 지수 백오프
                applyPerGameBackOff(gameId);
            }
        }
    }

    // 첫 due 계산:
    // - 아직 킥오프 전이면 planned(킥오프 시각)
    // - 시작 이후면 now + interval
    private Instant computeInitialDue(final Instant planned, final Instant now, final Duration interval) {
        return Optional.of(now)
                .filter(t -> !t.isBefore(planned))
                .map(t -> t.plus(interval))
                .orElse(planned);
    }

    // 오늘자 경기 목록 일괄 조회
    // - 예외 시 전역 백오프(지수) 적용 후 빈 맵 반환
    // - 성공 시 gameCode → 응답 매핑
    private Map<String, KboGameResponse> fetchGames(LocalDate date) {
        try {
            KboGamesResponse response = kboGameSyncClient.fetchGames(date);
            Map<String, KboGameResponse> games = new HashMap<>();
            for (KboGameResponse g : response.games()) {
                games.put(g.gameCode(), g);
            }
            return games;
        } catch (GameSyncException e) {
            applyGlobalBackOff();
            log.warn("fetchGames failed for {}: {}", date, e.toString());
            return Map.of();
        }
    }

    // 다음 실행 예정시각(due) 설정:
    // - 현재 시각 + 계산된 interval
    // - (지터 제거 정책 반영: 랜덤 오프셋 없음)
    private void scheduleNextRuns(Long gameId, Duration base) {
        Instant now = Instant.now(clock);
        Instant due = now.plus(base);
        nextRunAt.put(gameId, due);
    }

    private void clearGlobalBackoff() {
        globalBackoffUntil = Instant.EPOCH;
    }

    private boolean existsRemainGames(final LocalDate today) {
        return gameRepository.existsByDateAndGameStateIn(today, List.of(GameState.SCHEDULED, GameState.LIVE));
    }

    private Instant calculateTomorrow(LocalDate base) {
        return ZonedDateTime.of(base.plusDays(1), LocalTime.MIDNIGHT, clock.getZone()).toInstant();
    }

    // 자정(내일 00:00) Instant 계산
    private Instant computeStartInstant(LocalDate date, LocalTime startAt) {
        LocalTime effectiveStart = Objects.requireNonNullElse(startAt, DEFAULT_START_TIME);

        return ZonedDateTime.of(date, effectiveStart, clock.getZone()).toInstant();
    }

    // 폴링 주기 산정 로직
    // - SCHEDULED: 킥오프 전·지연 보호(10~15분)
    // - LIVE: 5분
    private Duration computePollingInterval(LocalDate date, LocalTime startAt, Instant now, GameState state) {
        Instant plannedStart = computeStartInstant(date, startAt);
        long elapsedMin = Duration.between(plannedStart, now).toMinutes();

        if (state == GameState.SCHEDULED) {
            if (elapsedMin < 120) {
                return Duration.ofMinutes(10);
            }
            return Duration.ofMinutes(15);
        }

        return Duration.ofMinutes(5);
    }

    private boolean isFinalState(GameState state) {
        return (state == GameState.COMPLETED || state == GameState.CANCELED);
    }

    private static Instant earliestOf(Instant a, Instant b) {
        if (a == null) {
            return b;
        }
        if (a.isBefore(b)) {
            return a;
        }
        return b;
    }

    private void resetGameFailureCount(Long gameId) {
        if (gameId != null) {
            failureCount.remove(gameId);
        }
    }

    // 개별 경기 실패 처리(지수 백오프):
    // - 실패 횟수에 따라 1,2,4,8분 … 단, cap=MAX_FAIL_MINUTE(8분)
    // - 다음 due를 백오프 후로 미룸
    // - 다회 실패 시 경고 로그
    private void applyPerGameBackOff(Long gameId) {
        int fail = failureCount.merge(gameId, 1, Integer::sum);
        int minutes = Math.min(MAX_FAIL_MINUTE, 1 << Math.min(fail, MAX_FAIL_COUNT));
        Instant due = Instant.now(clock).plus(Duration.ofMinutes(minutes));
        nextRunAt.put(gameId, due);
        if (fail > MAX_FAIL_COUNT) {
            log.warn("Poll repeatedly failed: gameId={}, failCount={}", gameId, fail);
        }
    }

    // 전역 fetch 실패 처리(전역 지수 백오프):
    // - 직전 남은 백오프(remain)를 기준으로 2배 증가(1 → 2 → 4 → 8)하되 cap=8분
    private void applyGlobalBackOff() {
        Instant now = Instant.now(clock);
        long remain = Math.max(0, Duration.between(now, globalBackoffUntil).toMinutes());
        long next = Math.min(MAX_FAIL_MINUTE, Math.max(1, remain == 0 ? 1 : remain * 2));
        globalBackoffUntil = now.plus(Duration.ofMinutes(next));
        log.warn("Daily fetch failed. Global backoff {} min until {}", next, globalBackoffUntil);
    }
}
