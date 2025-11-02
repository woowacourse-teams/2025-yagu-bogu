package com.yagubogu.game.event;

import com.yagubogu.game.service.GameEtlService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 경기 종료 이벤트 핸들러
 *
 * 경기가 종료되면 즉시 Bronze → Silver ETL을 실행하여
 * 최신 결과가 빠르게 사용자에게 반영되도록 함
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GameCompletedEventHandler {

    private final GameEtlService gameEtlService;

    @Async
    @EventListener
    public void handleGameFinalized(final GameFinalizedEvent event) {
        try {
            // 단일 게임 ETL
            gameEtlService.transformSpecificGame(
                    event.date(),
                    event.stadium(),
                    event.homeTeam(),
                    event.awayTeam(),
                    event.startTime()
            );

            log.info("[EVENT] Immediate ETL finalized: date={}, home={}, away={}",
                    event.date(), event.homeTeam(), event.awayTeam());
        } catch (Exception e) {
            log.error("[EVENT] Failed to process game finalized: date={}, home={}, away={}",
                    event.date(), event.homeTeam(), event.awayTeam(), e);
        }
    }
}
