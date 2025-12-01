package com.yagubogu.game.event;

import com.yagubogu.game.service.GameEtlService;
import com.yagubogu.global.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 경기 종료 이벤트 핸들러
 *
 * 경기가 종료되면 즉시 Bronze → Silver ETL을 실행하여
 * 최신 결과가 빠르게 사용자에게 반영되도록 함
 *
 * RabbitMQ를 통해 크롤링 서버로부터 메시지를 수신
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GameFinalizedEventHandler {

    private final GameEtlService gameEtlService;

    /**
     * RabbitMQ로부터 경기 종료 메시지 수신
     *
     * 크롤링 서버에서 경기가 종료되면 RabbitMQ를 통해 메시지가 전달되고,
     * 해당 경기에 대한 ETL을 즉시 실행
     */
    @RabbitListener(queues = RabbitMQConfig.GAME_FINALIZED_QUEUE)
    public void handleGameFinalizedFromRabbitMQ(final GameFinalizedEvent event) {
        try {
            log.info("[RABBITMQ] Received GameFinalizedEvent: date={}, home={}, away={}, state={}",
                    event.date(), event.homeTeam(), event.awayTeam(), event.state());

            // 단일 게임 ETL
            gameEtlService.transformSpecificGame(
                    event.date(),
                    event.stadium(),
                    event.homeTeam(),
                    event.awayTeam(),
                    event.startTime()
            );

            log.info("[RABBITMQ] ETL completed: date={}, home={}, away={}",
                    event.date(), event.homeTeam(), event.awayTeam());
        } catch (Exception e) {
            log.error("[RABBITMQ] Failed to process game finalized: date={}, home={}, away={}",
                    event.date(), event.homeTeam(), event.awayTeam(), e);
            // TODO : 에러 발생 시 메시지 - DLQ, Retry
        }
    }
}
