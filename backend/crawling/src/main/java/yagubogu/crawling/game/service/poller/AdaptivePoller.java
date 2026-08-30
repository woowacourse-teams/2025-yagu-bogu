package yagubogu.crawling.game.service.poller;

import com.yagubogu.game.domain.Game;
import com.yagubogu.game.domain.GameState;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import yagubogu.crawling.game.dto.GameCenter;
import yagubogu.crawling.game.dto.KboScoreboardGame;
import yagubogu.crawling.game.service.GameReadOnlyService;
import yagubogu.crawling.game.service.crawler.KboGameCenterCrawler.GameCenterSyncService;
import yagubogu.crawling.game.service.crawler.KboScoardboardCrawler.KboScoreboardService;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdaptivePoller {

    private final GameReadOnlyService gameReadOnlyService;
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
        List<Game> games = gameReadOnlyService.findAllByDate(today);
        scheduleManager.initialize(games, today);
        backoffStrategy.clearAll();
    }

    /**
     * 설정된 경기 폴링 주기마다 실행되는 메인 폴링 루프
     *
     * 동작 흐름:
     * 1. 전역 백오프 체크 (API 장애 시 중단)
     * 2. 웨이크업 시각 체크 (불필요한 폴링 방지)
     * 3. 게임센터 크롤링 (현재 이닝/타자/투수)
     * 4. 스코어보드 크롤링 (전체 경기 한번에)
     * 5. 각 경기별 업데이트 처리
     */
    @Scheduled(fixedDelayString = "${kbo.scheduler.polling-interval}")
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

        refreshGameCenter(today);

        Map<String, KboScoreboardGame> scoreboardGames = fetchScoreboard(today);
        if (scoreboardGames.isEmpty()) {
            log.debug("[POLLER] Skip: scoreboard empty");
            return;
        }

        globalBackoff.clear();
        processGames(today, scoreboardGames, now);
    }

    private void refreshGameCenter(final LocalDate date) {
        try {
            gameCenterSyncService.fetchGameCenter(date);
        } catch (Exception e) {
            log.warn("[POLLER] GameCenter refresh failed: date={}, message={}", date, e.getMessage());
        }
    }

    private void processGames(LocalDate today,
                              Map<String, KboScoreboardGame> scoreboardGames,
                              Instant now) {
        List<Game> games = gameReadOnlyService.findAllByDateWithStadium(today);

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
                    game.getGameCode());

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
                    .filter(game -> game.getGameCode() != null && !game.getGameCode().isBlank())
                    .collect(Collectors.toMap(
                            KboScoreboardGame::getGameCode,
                            game -> game
                    ));
        } catch (Exception e) {
            globalBackoff.applyBackoff();
            log.warn("[POLLER] Scoreboard fetch failed: {}", e.getMessage());
            return Map.of();
        }
    }

    private Game fetchFromGameCenter(Game game) {
        // GameCenter 원본 데이터 크롤링
        GameCenter gameCenter = gameCenterSyncService.fetchGameCenterOnly(game.getDate());

        // Bronze Layer에 저장 (ETL이 Silver로 변환)
        gameCenterSyncService.saveToBronzeLayer(gameCenter.getGames());

        // 메모리상 취소 여부 확인 (스케줄 관리용)
        return gameCenter.getGames().stream()
                .filter(detail -> detail.getGameCode().equals(game.getGameCode()))
                .findFirst()
                .map(detail -> {
                    game.updateGameState(parseGameState(detail.getGameSc()));
                    return game;
                })
                .orElse(game);
    }

    private GameState parseGameState(String gameSc) {
        if (gameSc == null || gameSc.isBlank()) {
            return GameState.SCHEDULED;
        }
        try {
            return GameState.fromNumber(Integer.parseInt(gameSc));
        } catch (NumberFormatException e) {
            return GameState.SCHEDULED;
        }
    }

    /**
     * 업데이트 필요 여부 판단
     *
     * 업데이트 케이스:
     * - 상태 변경 (SCHEDULED → LIVE → FINALIZED)
     * - LIVE 상태 (점수/이닝 변경 가능성)
     * - 같은 gameCode의 날짜/시각/구장/대진 정보 변경
     *
     * 생략 케이스:
     * - 상태와 경기 정보가 모두 동일한 SCHEDULED 경기
     */
    private void updateGameIfNeeded(Game game, KboScoreboardGame scoreboardGame) {
        GameState fetchedState = GameState.fromName(scoreboardGame.getStatus());

        boolean stateChanged = game.getGameState() != fetchedState;
        boolean isLive = fetchedState == GameState.LIVE;
        boolean metadataChanged = hasMetadataChanged(game, scoreboardGame);

        if (stateChanged || isLive || metadataChanged) {
            log.info("[UPDATE] Scoreboard changed: gameCode={}, stadium={}, home={}, away={}, "
                            + "startTime={}→{}, metadataChanged={}",
                    game.getGameCode(), scoreboardGame.getStadium(),
                    scoreboardGame.getHomeTeamScoreboard().name(),
                    scoreboardGame.getAwayTeamScoreboard().name(), game.getStartAt(),
                    scoreboardGame.getStartTime(), metadataChanged);
            kboScoreboardService.updateFromScoreboard(
                    game.getGameCode(),
                    scoreboardGame
            );
            game.updateGameState(fetchedState);
        }
    }

    private boolean hasMetadataChanged(final Game game, final KboScoreboardGame scoreboardGame) {
        return !Objects.equals(game.getDate(), scoreboardGame.getDate())
                || !Objects.equals(game.getStartAt(), scoreboardGame.getStartTime())
                || !Objects.equals(game.getStadium().getLocation(), scoreboardGame.getStadium())
                || !Objects.equals(game.getHomeTeam().getShortName(),
                scoreboardGame.getHomeTeamScoreboard().name())
                || !Objects.equals(game.getAwayTeam().getShortName(),
                scoreboardGame.getAwayTeamScoreboard().name());
    }

    private boolean isGameFinalized(KboScoreboardGame scoreboardGame) {
        return GameState.fromName(scoreboardGame.getStatus()).isFinalized();
    }

    private boolean hasRemainingGames(LocalDate today) {
        return gameReadOnlyService.existsByDateAndGameStateIn(
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
