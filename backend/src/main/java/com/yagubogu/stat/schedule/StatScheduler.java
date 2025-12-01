package com.yagubogu.stat.schedule;

import com.yagubogu.game.event.GameFinalizedEvent;
import com.yagubogu.global.config.RabbitMQConfig;
import com.yagubogu.stat.service.StatSyncService;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class StatScheduler {

    private final StatSyncService statSyncService;

    @Scheduled(cron = "0 0 3 * * *")
    public void updateVictoryRanking() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        triggerRankingUpdate(yesterday, "daily scheduler");
    }

    /**
     * RabbitMQ로부터 경기 종료 메시지 수신하여 랭킹 업데이트
     *
     * 크롤링 서버에서 경기가 종료되면 RabbitMQ를 통해 메시지가 전달되고,
     * 해당 날짜의 랭킹을 즉시 업데이트
     */
    @RabbitListener(queues = RabbitMQConfig.GAME_FINALIZED_QUEUE)
    public void handleGameFinalizedFromRabbitMQ(final GameFinalizedEvent event) {
        if (!event.state().isCompleted()) {
            log.debug("[RABBITMQ] Skip ranking update (state={}): date={}, home={}, away={}",
                    event.state(), event.date(), event.homeTeam(), event.awayTeam());
            return;
        }

        log.info("[RABBITMQ] Received GameFinalizedEvent for ranking update: date={}, home={}, away={}",
                event.date(), event.homeTeam(), event.awayTeam());
        triggerRankingUpdate(event.date(), "RabbitMQ game-finalized message");
    }

    private void triggerRankingUpdate(final LocalDate targetDate, final String triggerSource) {
        try {
            log.info("[STAT] Update victory ranking triggered by {} for date {}", triggerSource, targetDate);
            statSyncService.updateRankings(targetDate);
        } catch (RuntimeException e) {
            log.error("[{}]- {}", e.getClass().getSimpleName(), e.getMessage());
        }
    }
}
