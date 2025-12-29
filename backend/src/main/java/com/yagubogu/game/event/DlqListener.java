package com.yagubogu.game.event;

import com.yagubogu.global.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Dead Letter Queue 모니터링 리스너
 *
 * ETL 및 통계 업데이트 작업이 최대 재시도 횟수를 초과하여 실패한 경우
 * DLQ로 전송된 메시지를 수신하여 로그를 남긴다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DlqListener {

    /**
     * ETL DLQ 메시지 처리
     *
     * 경기 데이터 ETL 작업이 반복적으로 실패하여 DLQ로 이동된 메시지를 처리한다.
     */
    @RabbitListener(queues = RabbitMQConfig.GAME_FINALIZED_ETL_DLQ)
    public void handleEtlDlq(final Message message) {
        logDlqMessage("ETL", message);
    }

    /**
     * Stats DLQ 메시지 처리
     *
     * 통계 업데이트 작업이 반복적으로 실패하여 DLQ로 이동된 메시지를 처리한다.
     */
    @RabbitListener(queues = RabbitMQConfig.GAME_FINALIZED_STATS_DLQ)
    public void handleStatsDlq(final Message message) {
        logDlqMessage("STATS", message);
    }

    private void logDlqMessage(final String queueType, final Message message) {
        String messageBody = new String(message.getBody());
        Object retryAttempt = message.getMessageProperties().getHeaders().get("x-retry-attempt");
        Object deathReason = message.getMessageProperties().getHeaders().get("x-first-death-reason");
        Object deathQueue = message.getMessageProperties().getHeaders().get("x-first-death-queue");
        Object deathExchange = message.getMessageProperties().getHeaders().get("x-first-death-exchange");

        log.error("""
                [DLQ-{}] Message sent to Dead Letter Queue
                ┌─────────────────────────────────────────────────────────────
                │ Retry Attempts: {}
                │ Death Reason: {}
                │ Original Queue: {}
                │ Original Exchange: {}
                │ Headers: {}
                │ Body: {}
                └─────────────────────────────────────────────────────────────
                """,
                queueType,
                retryAttempt != null ? retryAttempt : "N/A",
                deathReason != null ? deathReason : "N/A",
                deathQueue != null ? deathQueue : "N/A",
                deathExchange != null ? deathExchange : "N/A",
                message.getMessageProperties().getHeaders(),
                messageBody
        );
    }
}
