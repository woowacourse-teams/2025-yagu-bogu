package com.yagubogu.game.event;

import com.rabbitmq.client.Channel;
import com.yagubogu.game.service.GameEtlService;
import com.yagubogu.global.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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

    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final String RETRY_HEADER = "x-retry-attempt";

    private final GameEtlService gameEtlService;
    private final RabbitTemplate rabbitTemplate;

    /**
     * RabbitMQ로부터 경기 종료 메시지 수신하여 ETL 실행
     *
     * 크롤링 서버에서 경기가 종료되면 RabbitMQ를 통해 메시지가 전달되고,
     * 해당 경기에 대한 ETL을 즉시 실행. 실패 시 재시도 후 DLQ로 전송
     */
    @RabbitListener(queues = RabbitMQConfig.GAME_FINALIZED_ETL_QUEUE, ackMode = "MANUAL")
    public void handleGameFinalizedFromRabbitMQ(final GameFinalizedEvent event,
                                                final Message message,
                                                final Channel channel) throws Exception {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        int retryAttempt = getRetryAttempt(message);

        try {
            log.info("[RABBITMQ] Received GameFinalizedEvent for ETL (attempt {}): date={}, home={}, away={}, state={}",
                    retryAttempt, event.date(), event.homeTeam(), event.awayTeam(), event.state());

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

            // ETL 완료 후 통계 업데이트를 위한 이벤트 발행
            ETLCompletedEvent etlCompletedEvent = new ETLCompletedEvent(
                    event.date(),
                    event.stadium(),
                    event.homeTeam(),
                    event.awayTeam()
            );
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.GAME_FINALIZED_EXCHANGE,
                    RabbitMQConfig.GAME_FINALIZED_STATS_ROUTING_KEY,
                    etlCompletedEvent
            );
            log.info("[RABBITMQ] Published ETLCompletedEvent for stats: date={}, home={}, away={}",
                    event.date(), event.homeTeam(), event.awayTeam());

            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            if (retryAttempt >= MAX_RETRY_ATTEMPTS) {
                log.error("[RABBITMQ] ETL failed after {} attempts, sending to DLQ: date={}, home={}, away={}",
                        retryAttempt, event.date(), event.homeTeam(), event.awayTeam(), e);
                channel.basicNack(deliveryTag, false, false); // DLQ로 이동
                return;
            }

            int nextAttempt = retryAttempt + 1;
            log.warn("[RABBITMQ] ETL failed (attempt {}), schedule retry via delay queue: date={}, home={}, away={}",
                    nextAttempt, event.date(), event.homeTeam(), event.awayTeam(), e);
            scheduleRetry(event, nextAttempt);
            channel.basicAck(deliveryTag, false); // 메시지를 수동으로 제거 후 지연 큐로 재전송
        }
    }

    private void scheduleRetry(final GameFinalizedEvent event, final int nextAttempt) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.GAME_FINALIZED_EXCHANGE,
                RabbitMQConfig.GAME_FINALIZED_ETL_DELAY_ROUTING_KEY,
                event,
                msg -> {
                    msg.getMessageProperties().setHeader(RETRY_HEADER, nextAttempt);
                    msg.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                    return msg;
                });
    }

    private int getRetryAttempt(final Message message) {
        Object retryCount = message.getMessageProperties().getHeaders().get(RETRY_HEADER);
        if (retryCount instanceof Integer) {
            return (Integer) retryCount;
        }
        if (retryCount instanceof Long) {
            return ((Long) retryCount).intValue();
        }
        return 0;
    }
}
