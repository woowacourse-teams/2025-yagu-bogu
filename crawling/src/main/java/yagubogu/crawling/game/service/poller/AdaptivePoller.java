package yagubogu.crawling.game.service.poller;

import com.yagubogu.game.domain.Game;
import com.yagubogu.game.domain.GameState;
import com.yagubogu.game.repository.GameRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
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

    private final GameRepository gameRepository;
    private final KboScoreboardService kboScoreboardService;
    private final GameCenterSyncService gameCenterSyncService;
    private final GameScheduleManager scheduleManager;
    private final BackoffStrategy backoffStrategy;
    private final GlobalBackOffManager globalBackoff;
    private final Clock clock;

    /**
     * 00시 스케줄러에서 호출
     */
    public void initializeTodaySchedule(LocalDate today) {
        List<Game> games = gameRepository.findAllByDate(today);
        scheduleManager.initialize(games, today);
        backoffStrategy.clearAll();
    }

    /**
     * 1분마다 실행되는 메인 폴링 루프
     *
     * 동작 흐름:
     * 1. 전역 백오프 체크 (API 장애 시 중단)
     * 2. 웨이크업 시각 체크 (불필요한 폴링 방지)
     * 3. 스코어보드 크롤링 (전체 경기 한번에)
     * 4. 각 경기별 업데이트 처리
     */
    @Scheduled(fixedDelay = 60_000)
    public void pollGameWhenReachDue() {
        Instant now = Instant.now(clock);

        if (globalBackoff.isActive(now)) {
            log.debug("[POLLER] Skip: global backoff active");
            return;
        }

        if (!scheduleManager.shouldWake(now)) {
            log.debug("[POLLER] Skip: not wake time yet");
            return;
        }

        LocalDate today = LocalDate.now(clock);
        if (!hasRemainingGames(today)) {
            log.info("[POLLER] No remaining games for today");
            cleanupAndSleepUntilTomorrow(today);
            return;
        }

        Map<String, KboScoreboardGame> scoreboardGames = fetchScoreboard(today);
        if (scoreboardGames.isEmpty()) {
            log.debug("[POLLER] Skip: scoreboard empty");
            return;
        }

        globalBackoff.clear();
        processGames(today, scoreboardGames, now);
    }

    private void processGames(LocalDate today,
                              Map<String, KboScoreboardGame> scoreboardGames,
                              Instant now) {
        List<Game> games = gameRepository.findAllByDateWithStadium(today);

        for (Game game : games) {
            if (game.getGameState().isFinalized()) {
                removeGameFromSchedule(game.getId());
                continue;
            }

            if (!scheduleManager.shouldPollGame(game.getId(), now)) {
                continue;
            }

            processGame(game, scoreboardGames);
        }
    }

    /**
     * 개별 경기 처리
     *
     * 흐름:
     * 1. 스코어보드에서 경기 찾기
     * 2. 없으면 게임센터 확인 (취소 여부)
     * 3. 업데이트 필요 시 DB 반영
     * 4. 다음 폴링 예약
     */
    private void processGame(Game game, Map<String, KboScoreboardGame> scoreboardGames) {
        try {
            KboScoreboardGame scoreboardGame = scoreboardGames.get(
                    makeGameKey(game.getDate(), game.getStartAt(), game.getStadium().getLocation()));

            if (scoreboardGame == null) {
                handleMissingGame(game);
                return;
            }

            updateGameIfNeeded(game, scoreboardGame);
            backoffStrategy.resetFailureCount(game.getId());

            if (isGameFinalized(scoreboardGame)) {
                removeGameFromSchedule(game.getId());
                log.info("[POLLER] Game finalized: {}", game.getGameCode());
            } else {
                scheduleManager.scheduleNextPoll(game.getId());
            }

        } catch (Exception e) {
            log.error("[POLLER] Failed to process game: {}", game.getId(), e);
            handleGameFailure(game);
        }
    }

    /**
     * 스코어보드에 없는 경기 처리
     *
     * 게임센터에서 취소 여부 확인 후:
     * - 취소: 스케줄에서 제거
     * - 지연: 재시도 예약
     */
    private void handleMissingGame(Game game) {
        Game gameCenterData = fetchFromGameCenter(game);

        if (gameCenterData.getGameState().isCanceled()) {
            removeGameFromSchedule(game.getId());
            log.debug("[POLLER] Game canceled: {}", game.getGameCode());
        } else {
            handleGameFailure(game);
        }
    }

    private void handleGameFailure(Game game) {
        Instant nextRetryTime = backoffStrategy.applyGameBackoff(game.getId());

        if (nextRetryTime == null) {
            handleMissingGame(game);
        } else {
            scheduleManager.scheduleNextPollAt(game.getId(), nextRetryTime);
        }
    }

    /**
     * 스코어보드 전체 크롤링
     *
     * 실패 시: 전역 백오프 적용 (API 차단 방지)
     */
    private Map<String, KboScoreboardGame> fetchScoreboard(LocalDate date) {
        try {
            List<KboScoreboardGame> scoreboardResponses = kboScoreboardService.fetchScoreboardOnly(date);

            return scoreboardResponses.stream()
                    .collect(Collectors.toMap(
                            game -> makeGameKey(game.getDate(), game.getStartTime(), game.getStadium()),
                            game -> game
                    ));
        } catch (Exception e) {
            globalBackoff.applyBackoff();
            log.warn("[POLLER] Scoreboard fetch failed: {}", e.getMessage());
            return Map.of();
        }
    }

    private String makeGameKey(LocalDate date, LocalTime startAt, String stadium) {
        return String.format("%s_%s_%s", date, startAt, stadium);
    }

    private Game fetchFromGameCenter(Game game) {
        List<Game> games = gameCenterSyncService.fetchGameCenter(game.getDate());

        return games.stream()
                .filter(g -> g.getGameCode().equals(game.getGameCode()))
                .findFirst()
                .orElse(game);
    }

    /**
     * 업데이트 필요 여부 판단
     *
     * 업데이트 케이스:
     * - 상태 변경 (SCHEDULED → LIVE → FINALIZED)
     * - LIVE 상태 (점수/이닝 변경 가능성)
     *
     * 생략 케이스:
     * - SCHEDULED 상태 유지 (변경 사항 없음)
     */
    private void updateGameIfNeeded(Game game, KboScoreboardGame scoreboardGame) {
        GameState fetchedState = GameState.fromName(scoreboardGame.getStatus());

        boolean stateChanged = game.getGameState() != fetchedState;
        boolean isLive = fetchedState == GameState.LIVE;

        if (stateChanged || isLive) {
            log.debug("[UPDATE] Calling updateFromScoreboard for gameCode={}, stadium={}, home={}, away={}",
                    game.getGameCode(), scoreboardGame.getStadium(),
                    scoreboardGame.getHomeTeamScoreboard().name(),
                    scoreboardGame.getAwayTeamScoreboard().name());
            kboScoreboardService.updateFromScoreboard(
                    game.getGameCode(),
                    scoreboardGame
            );
            game.updateGameState(fetchedState);
        }
    }

    private boolean isGameFinalized(KboScoreboardGame scoreboardGame) {
        return GameState.fromName(scoreboardGame.getStatus()).isFinalized();
    }

    private boolean hasRemainingGames(LocalDate today) {
        return gameRepository.existsByDateAndGameStateIn(
                today,
                List.of(GameState.SCHEDULED, GameState.LIVE)
        );
    }

    private void removeGameFromSchedule(Long gameId) {
        scheduleManager.removeGame(gameId);
        backoffStrategy.resetFailureCount(gameId);
    }

    private void cleanupAndSleepUntilTomorrow(LocalDate today) {
        scheduleManager.clearAll();
        backoffStrategy.clearAll();
        scheduleManager.sleepUntilTomorrow(today);
    }
}
