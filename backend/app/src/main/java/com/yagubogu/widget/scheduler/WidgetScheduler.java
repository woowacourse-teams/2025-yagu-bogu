package com.yagubogu.widget.scheduler;

import com.yagubogu.game.domain.Game;
import com.yagubogu.game.domain.GameState;
import com.yagubogu.game.repository.GameRepository;
import com.yagubogu.widget.domain.WidgetGamePush;
import com.yagubogu.widget.repository.WidgetGamePushRepository;
import com.yagubogu.widget.service.WidgetService;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 위젯 스케줄러 — 경기 시작/종료에 따른 푸시 발송을 관리합니다.
 *
 * <h3>START 트리거 규칙</h3>
 * <ul>
 *   <li>경기 시작 30분 전(±1분 윈도우)에 START 푸시 발송</li>
 *   <li>더블헤더 안전장치: 같은 팀의 경기가 이미 LIVE 상태면 START 발송을 건너뜁니다.
 *       앞 경기가 종료되면 지연 시작 체크가 자동으로 처리합니다.</li>
 * </ul>
 *
 * <h3>지연 START (더블헤더)</h3>
 * <ul>
 *   <li>START 30분 전 윈도우를 이미 지났지만 아직 START를 못 보낸 경기 중,
 *       같은 팀의 모든 앞선 경기가 종료되면 즉시 START 발송</li>
 * </ul>
 *
 * <h3>END 트리거</h3>
 * <ul>
 *   <li>START가 발송된 경기가 COMPLETED/CANCELED 되면 END 발송</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WidgetScheduler {

    /** START 푸시를 보낼 경기 시작 전 시간 (분) */
    private static final int START_PUSH_BEFORE_MINUTES = 30;

    /** 스케줄러 실행 주기와 맞춘 윈도우 허용 범위 (분) */
    private static final int WINDOW_MINUTES = 1;

    private final GameRepository gameRepository;
    private final WidgetGamePushRepository widgetGamePushRepository;
    private final WidgetService widgetService;
    private final Clock clock;

    /**
     * 1분마다 실행:
     * 1. 30분 후 시작 예정인 SCHEDULED 경기에 START 푸시
     * 2. 이미 30분 전 윈도우를 지났지만 아직 START 못 한 경기(더블헤더 지연) 처리
     * 3. 종료된 경기에 END 푸시
     */
    @Scheduled(fixedDelay = 60_000, initialDelay = 5_000)
    public void run() {
        final LocalDateTime now = LocalDateTime.now(clock);
        final LocalDate today = now.toLocalDate();
        final LocalTime nowTime = now.toLocalTime();

        try {
            triggerScheduledStarts(today, nowTime);
        } catch (Exception e) {
            log.error("[WIDGET-SCHEDULER] START trigger failed", e);
        }

        try {
            triggerDeferredStarts(today, nowTime);
        } catch (Exception e) {
            log.error("[WIDGET-SCHEDULER] Deferred START trigger failed", e);
        }

        try {
            triggerEnds(today);
        } catch (Exception e) {
            log.error("[WIDGET-SCHEDULER] END trigger failed", e);
        }
    }

    /**
     * 경기 시작 30분 전 START 푸시.
     * 더블헤더 안전장치: 같은 팀이 이미 LIVE 중이면 건너뜁니다.
     */
    private void triggerScheduledStarts(final LocalDate today, final LocalTime nowTime) {
        final LocalTime from = nowTime.plusMinutes(START_PUSH_BEFORE_MINUTES - WINDOW_MINUTES);
        final LocalTime to = nowTime.plusMinutes(START_PUSH_BEFORE_MINUTES + WINDOW_MINUTES);

        final List<Game> candidates = gameRepository.findScheduledGamesStartingBetween(
                today, GameState.SCHEDULED, from, to);

        for (final Game game : candidates) {
            // 더블헤더 안전장치: 홈/원정팀 중 하나라도 LIVE 경기가 있으면 지연
            final boolean homeTeamLive = gameRepository.existsLiveGameForTeam(today, game.getHomeTeam());
            final boolean awayTeamLive = gameRepository.existsLiveGameForTeam(today, game.getAwayTeam());

            if (homeTeamLive || awayTeamLive) {
                log.info("[WIDGET-SCHEDULER] Deferring START for gameId={} (previous game still LIVE)",
                        game.getId());
                continue;
            }

            log.info("[WIDGET-SCHEDULER] Sending START for gameId={} startAt={}", game.getId(), game.getStartAt());
            widgetService.sendStartPush(game);
        }
    }

    /**
     * 더블헤더 지연 시작: START 30분 전 윈도우를 지났지만 아직 START 미발송인 경기 처리.
     *
     * <p>조건:
     * <ul>
     *   <li>오늘 경기 중 SCHEDULED 상태</li>
     *   <li>startAt + WINDOW ≤ now (30분 전 윈도우 이미 지남)</li>
     *   <li>widget_game_pushes에 start_sent_at = null</li>
     *   <li>같은 팀의 모든 앞선 경기(startAt < 본 경기)가 COMPLETED/CANCELED</li>
     * </ul>
     */
    private void triggerDeferredStarts(final LocalDate today, final LocalTime nowTime) {
        // START 윈도우를 이미 지난 SCHEDULED 경기: startAt < now - (START_PUSH_BEFORE_MINUTES - WINDOW)
        final LocalTime cutoff = nowTime.minusMinutes(START_PUSH_BEFORE_MINUTES - WINDOW_MINUTES);

        final List<Game> pastWindowScheduled = gameRepository.findScheduledGamesStartingBetween(
                today, GameState.SCHEDULED, LocalTime.MIDNIGHT, cutoff);

        // START 미발송인 것만 필터링
        final List<Long> alreadySentIds = widgetGamePushRepository.findAll().stream()
                .filter(WidgetGamePush::isStartSent)
                .map(WidgetGamePush::getGameId)
                .toList();

        final List<Game> pending = pastWindowScheduled.stream()
                .filter(g -> !alreadySentIds.contains(g.getId()))
                .toList();

        for (final Game game : pending) {
            // 앞 경기가 아직 LIVE면 계속 대기
            final boolean homeTeamLive = gameRepository.existsLiveGameForTeam(today, game.getHomeTeam());
            final boolean awayTeamLive = gameRepository.existsLiveGameForTeam(today, game.getAwayTeam());

            if (homeTeamLive || awayTeamLive) {
                log.debug("[WIDGET-SCHEDULER] Deferred START still waiting for gameId={}", game.getId());
                continue;
            }

            log.info("[WIDGET-SCHEDULER] Sending deferred START for gameId={} (double-header)",
                    game.getId());
            widgetService.sendStartPush(game);
        }
    }

    /**
     * START가 발송된 경기 중 COMPLETED/CANCELED 상태가 된 경기에 END 푸시를 발송합니다.
     */
    private void triggerEnds(final LocalDate today) {
        final List<WidgetGamePush> startSentNotEnded = widgetGamePushRepository.findAll().stream()
                .filter(p -> p.isStartSent() && !p.isEndSent())
                .toList();

        if (startSentNotEnded.isEmpty()) {
            return;
        }

        final List<Long> gameIds = startSentNotEnded.stream()
                .map(WidgetGamePush::getGameId)
                .toList();

        final List<Game> finalizedGames = gameRepository.findFinalizedGamesById(
                today, List.of(GameState.COMPLETED, GameState.CANCELED), gameIds);

        for (final Game game : finalizedGames) {
            log.info("[WIDGET-SCHEDULER] Sending END for gameId={} state={}", game.getId(), game.getGameState());
            widgetService.sendEndPush(game);
        }
    }
}
