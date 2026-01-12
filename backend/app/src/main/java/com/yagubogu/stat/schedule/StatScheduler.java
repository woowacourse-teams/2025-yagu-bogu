package com.yagubogu.stat.schedule;

import com.yagubogu.game.event.GameFinalizedEvent;
import com.yagubogu.stat.service.StatSyncService;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

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

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleGameFinalizedEvent(final GameFinalizedEvent event) {
        if (!event.state().isCompleted()) {
            log.debug("[EVENT] Skip ranking update (state={}): date={}, home={}, away={}",
                    event.state(), event.date(), event.homeTeam(), event.awayTeam());
            return;
        }

        triggerRankingUpdate(event.date(), "game-finalized event");
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
