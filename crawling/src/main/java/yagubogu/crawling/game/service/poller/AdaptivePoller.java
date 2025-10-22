package yagubogu.crawling.game.service.poller;

import com.yagubogu.game.domain.Game;
import com.yagubogu.game.domain.GameState;
import com.yagubogu.game.repository.GameRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import yagubogu.crawling.game.dto.KboScoreboardGame;
import yagubogu.crawling.game.service.crawler.KboGameCenterCrawler.GameCenterSyncService;
import yagubogu.crawling.game.service.crawler.KboScoardboardCrawler.KboScoreboardService;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdaptivePoller {

    private static final LocalTime DEFAULT_START_TIME = LocalTime.of(18, 30);
    private static final int MAX_FAIL_MINUTE = 8;
    private static final Duration POLLING_INTERVAL = Duration.ofMinutes(1);
    private static final int QUICK_RETRY_THRESHOLD = 5;
    private static final int QUICK_RETRY_INTERVAL = 2;
    private static final int MAX_RETRY_COUNT = 15;

    private final GameRepository gameRepository;
    private final KboScoreboardService kboScoreboardService;
    private final GameCenterSyncService gameCenterSyncService;
    private final Clock clock;

    private final Map<Long, Integer> failureCount = new ConcurrentHashMap<>();
    private volatile Instant globalBackoffUntil = Instant.EPOCH;
    private volatile Instant globalWakeAt = Instant.MAX;
    private final Map<Long, Instant> nextRunAt = new ConcurrentHashMap<>();

    /**
     * 오늘 경기 스케줄 초기화
     * 00시 스케줄러에서 호출
     */
    public void initializeTodaySchedule(LocalDate today) {
        List<Game> games = gameRepository.findAllByDate(today);
        log.info("[DEBUG] today={}, games.size()={}", today, games.size());

        nextRunAt.clear();

        if (games.isEmpty()) {
            globalWakeAt = calculateTomorrow(today);
            log.info("[ADAPTIVE_POLLER] No games today, sleep until tomorrow");
            return;
        }

        Instant earliestDue = null;
        Instant now = Instant.now(clock);

        for (Game g : games) {
            if (g.getGameState().isFinalized()) {
                continue;
            }

            Instant planned = computeStartInstant(g.getDate(), g.getStartAt());
            Instant due = computeInitialDue(planned, now);

            nextRunAt.put(g.getId(), due);
            earliestDue = earliestOf(earliestDue, due);
        }

        globalWakeAt = Optional.ofNullable(earliestDue)
                .filter(d -> d.isAfter(now))
                .orElseGet(() -> {
                    log.info("[ADAPTIVE_POLLER] All games finalized, sleep until tomorrow");
                    return calculateTomorrow(today);
                });

        log.info("[ADAPTIVE_POLLER] Initialized {} games, next wake at {}",
                nextRunAt.size(), globalWakeAt.atZone(clock.getZone()));
    }

    /**
     * 1분 간격 폴링 메인 루프
     */
    @Scheduled(fixedDelay = 60_000)
    public void pollGameWhenReachDue() {
        Instant now = Instant.now(clock);

        // 전역 백오프 체크
        if (now.isBefore(globalBackoffUntil)) {
            return;
        }

        // 웨이크업 시각 체크
        if (now.isBefore(globalWakeAt)) {
            return;
        }

        LocalDate today = LocalDate.now(clock);

        // 남은 경기 체크
        if (!existsRemainGames(today)) {
            nextRunAt.clear();
            failureCount.clear();
            globalWakeAt = calculateTomorrow(today);
            log.info("[ADAPTIVE_POLLER] No remaining games, sleep until tomorrow");
            return;
        }

        // 스코어보드 크롤링 (조회만)
        Map<String, KboScoreboardGame> dailyGames = fetchScoreboardGames(today);
        if (dailyGames.isEmpty()) {
            return;
        }

        clearGlobalBackoff();

        // 경기별 처리
        for (Game game : gameRepository.findAllByDate(today)) {
            Long gameId = game.getId();

            // 종료된 경기 제거
            if (game.getGameState().isFinalized()) {
                nextRunAt.remove(gameId);
                failureCount.remove(gameId);
                continue;
            }

            // Due 체크
            Instant dueAt = nextRunAt.get(gameId);
            if (dueAt == null) {
                // 스케줄에 없는 경기 → 초기화 필요
                Instant planned = computeStartInstant(game.getDate(), game.getStartAt());
                dueAt = computeInitialDue(planned, now);
                nextRunAt.put(gameId, dueAt);
            }

            if (now.isBefore(dueAt)) {
                continue;
            }

            try {
                String gameCode = game.getGameCode();
                KboScoreboardGame fetchedGame = dailyGames.get(gameCode);
                if (fetchedGame == null) {
                    // 게임 센터에서 취소 여부 확인
                    fetchGameCenter(game);

                    applyPerGameBackOff(game);
                    continue;
                }

                // 업데이트 필요 여부 판단
                if (shouldUpdate(game, fetchedGame)) {
                    kboScoreboardService.updateFromScoreboard(
                            game.getGameCode(),
                            fetchedGame
                    );
                }

                resetGameFailureCount(gameId);

                // 종료 상태면 스케줄 제거
                GameState newState = GameState.fromStatus(fetchedGame.getStatus());
                if (newState.isFinalized()) {
                    nextRunAt.remove(gameId);
                    failureCount.remove(gameId);
                    log.info("[ADAPTIVE_POLLER] Game finalized, removed from schedule: {}",
                            game.getGameCode());
                    continue;
                }

                // 다음 due (1분 고정)
                scheduleNextRuns(gameId);

            } catch (Exception e) {
                log.error("[ADAPTIVE_POLLER] Game poll failed: gameId={}", gameId, e);
                applyPerGameBackOff(game);
            }
        }
    }

    private void fetchGameCenter(final Game game) {
        List<Game> games = gameCenterSyncService.fetchGameCenter(game.getDate());
        Game gameCenter = games.stream()
                .filter(g -> g.getGameCode().equals(game.getGameCode()))
                .findAny()
                .orElse(null);

        Long gameId = game.getId();
        if (gameCenter.getGameState().isCanceled()) {
            // 취소 경기 → 더 이상 확인 안 함
            nextRunAt.remove(gameId);
            failureCount.remove(gameId);
        } else {
            // 일시적 지연 → 짧은 재시도
            applyPerGameBackOff(game);
        }
    }

    /**
     * 스코어보드 크롤링 (조회만, 트랜잭션 X)
     */
    private Map<String, KboScoreboardGame> fetchScoreboardGames(LocalDate date) {
        try {
            List<KboScoreboardGame> games = kboScoreboardService.fetchScoreboardOnly(date);

            return games.stream()
                    .collect(Collectors.toMap(
                            KboScoreboardGame::getGameCode,
                            g -> g
                    ));
        } catch (Exception e) {
            applyGlobalBackOff();
            log.warn("[ADAPTIVE_POLLER] fetchScoreboardGames failed for {}: {}",
                    date, e.getMessage());
            return Map.of();
        }
    }

    /**
     * 업데이트 필요 여부 판단
     * - 상태 변경: 무조건 업데이트
     * - LIVE: 무조건 업데이트 (점수 변경 가능성)
     * - SCHEDULED: skip
     */
    private boolean shouldUpdate(Game current, KboScoreboardGame fetched) {
        GameState fetchedState = GameState.fromStatus(fetched.getStatus());

        // 상태 변경
        if (current.getGameState() != fetchedState) {
            return true;
        }

        // LIVE 경기는 무조건 (점수/이닝 변경 가능성)
        if (current.getGameState() == GameState.LIVE) {
            return true;
        }

        return false;
    }

    private Instant computeInitialDue(final Instant planned, final Instant now) {
        return Optional.of(now)
                .filter(t -> !t.isBefore(planned))
                .map(t -> t.plus(POLLING_INTERVAL))
                .orElse(planned);
    }

    private void scheduleNextRuns(Long gameId) {
        Instant now = Instant.now(clock);
        Instant due = now.plus(POLLING_INTERVAL);
        nextRunAt.put(gameId, due);
    }

    private void clearGlobalBackoff() {
        globalBackoffUntil = Instant.EPOCH;
    }

    private boolean existsRemainGames(final LocalDate today) {
        return gameRepository.existsByDateAndGameStateIn(
                today,
                List.of(GameState.SCHEDULED, GameState.LIVE)
        );
    }

    private Instant calculateTomorrow(LocalDate base) {
        return ZonedDateTime.of(base.plusDays(1), LocalTime.MIDNIGHT, clock.getZone())
                .toInstant();
    }

    private Instant computeStartInstant(LocalDate date, LocalTime startAt) {
        LocalTime effectiveStart = Objects.requireNonNullElse(startAt, DEFAULT_START_TIME);
        return ZonedDateTime.of(date, effectiveStart, clock.getZone()).toInstant();
    }

    private static Instant earliestOf(Instant a, Instant b) {
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }
        return a.isBefore(b) ? a : b;
    }

    private void resetGameFailureCount(Long gameId) {
        if (gameId != null) {
            failureCount.remove(gameId);
        }
    }

    private void applyPerGameBackOff(final Game game) {
        Long gameId = game.getId();
        int fail = failureCount.merge(gameId, 1, Integer::sum);

        // Phase 1: 초반 5회 - 고정 2분 간격 (빠른 복구 감지)
        if (fail <= QUICK_RETRY_THRESHOLD) {
            Instant due = Instant.now(clock).plus(Duration.ofMinutes(QUICK_RETRY_INTERVAL));
            nextRunAt.put(gameId, due);

            log.debug("[ADAPTIVE_POLLER] Quick retry: gameId={}, failCount={}, nextRetry={}",
                    gameId, fail, due.atZone(clock.getZone()));
            return;
        }

        // Phase 2: 5회 실패 시점 - 게임 센터 확인 (취소 여부 판단)
        if (fail == QUICK_RETRY_THRESHOLD + 1) {
            fetchGameCenter(game);
            return;
        }

        // Phase 3: 6회 이상 - 제한적 백오프 (영구 실패로 간주)
        int backoffMultiplier = Math.min(fail - QUICK_RETRY_THRESHOLD, 3);
        int minutes = Math.min(MAX_FAIL_MINUTE, 1 << backoffMultiplier);
        Instant due = Instant.now(clock).plus(Duration.ofMinutes(minutes));
        nextRunAt.put(gameId, due);

        // 최대 재시도 초과 시 경고
        if (fail > MAX_RETRY_COUNT) {
            log.warn("[ADAPTIVE_POLLER] Excessive failures, consider manual check: " +
                            "gameId={}, gameCode={}, failCount={}",
                    gameId, game.getGameCode(), fail);
        }
    }

    private void applyGlobalBackOff() {
        Instant now = Instant.now(clock);
        long remain = Math.max(0, Duration.between(now, globalBackoffUntil).toMinutes());
        long next = Math.min(MAX_FAIL_MINUTE, Math.max(1, remain == 0 ? 1 : remain * 2));
        globalBackoffUntil = now.plus(Duration.ofMinutes(next));

        log.warn("[ADAPTIVE_POLLER] Daily fetch failed. Global backoff {} min until {}",
                next, globalBackoffUntil);
    }
}
