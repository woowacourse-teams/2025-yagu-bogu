package com.yagubogu.stat.schedule;

import com.yagubogu.game.event.ETLCompletedEvent;
import com.yagubogu.global.config.RabbitMQConfig;
import com.yagubogu.stat.service.StatSyncService;
import com.rabbitmq.client.Channel;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class StatScheduler {

    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final String RETRY_HEADER = "x-retry-attempt";

    private final StatSyncService statSyncService;
    private final RabbitTemplate rabbitTemplate;

    @Scheduled(cron = "0 0 3 * * *")
    public void updateVictoryRanking() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        try {
            triggerRankingUpdate(yesterday, "daily scheduler");
        } catch (RuntimeException e) {
            log.error("[RABBITMQ] Daily ranking update failed: {}", e.getMessage(), e);
        }
    }

    /**
     * ETL 완료 메시지 수신하여 랭킹 업데이트
     *
     * ETL이 완료되면 RabbitMQ를 통해 메시지가 전달되고,
     * 해당 날짜의 랭킹을 즉시 업데이트
     */
    @RabbitListener(queues = RabbitMQConfig.GAME_FINALIZED_STATS_QUEUE, ackMode = "MANUAL")
    public void handleETLCompletedFromRabbitMQ(final ETLCompletedEvent event,
                                               final Message message,
                                               final Channel channel) throws Exception {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        int retryAttempt = getRetryAttempt(message);

        try {
            log.info("[RABBITMQ] Received ETLCompletedEvent for ranking update: date={}, home={}, away={}",
                    event.date(), event.homeTeam(), event.awayTeam());
            triggerRankingUpdate(event.date(), "RabbitMQ ETL-completed message");
            channel.basicAck(deliveryTag, false);
        } catch (RuntimeException e) {
            if (retryAttempt >= MAX_RETRY_ATTEMPTS) {
                log.error("[RABBITMQ] Ranking update failed after {} attempts, sending to DLQ: date={}, home={}, away={}",
                        retryAttempt, event.date(), event.homeTeam(), event.awayTeam(), e);
                channel.basicNack(deliveryTag, false, false);
                return;
            }

            int nextAttempt = retryAttempt + 1;
            log.warn("[RABBITMQ] Ranking update failed (attempt {}), schedule retry via delay queue: date={}, home={}, away={}",
                    nextAttempt, event.date(), event.homeTeam(), event.awayTeam(), e);
            scheduleRetry(event, nextAttempt);
            channel.basicAck(deliveryTag, false);
        }
    }

    private void triggerRankingUpdate(final LocalDate targetDate, final String triggerSource) {
        log.info("[STAT] Update victory ranking triggered by {} for date {}", triggerSource, targetDate);
        statSyncService.updateRankings(targetDate);
    }

    private void scheduleRetry(final ETLCompletedEvent event, final int nextAttempt) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.GAME_FINALIZED_EXCHANGE,
                RabbitMQConfig.GAME_FINALIZED_STATS_DELAY_ROUTING_KEY,
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
