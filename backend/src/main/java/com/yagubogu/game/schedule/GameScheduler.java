package com.yagubogu.game.schedule;

import com.yagubogu.game.exception.GameSyncException;
import com.yagubogu.game.service.GameResultSyncService;
import com.yagubogu.game.service.GameScheduleSyncService;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class GameScheduler {

    private final GameScheduleSyncService gameScheduleSyncService;
    private final GameResultSyncService gameResultSyncService;

    @Scheduled(cron = "0 0 0 * * *")
    public void fetchDailyGameSchedule() {
        LocalDate today = LocalDate.now();
        try {
            gameScheduleSyncService.syncGameSchedule(today);
        } catch (GameSyncException e) {
            log.error("[GameSyncException]- {}", e.getMessage());
        }
    }

    @Scheduled(cron = "0 40 2 * * *")
    public void fetchDailyGameResult() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        try {
            gameResultSyncService.syncGameResult(yesterday);
        } catch (GameSyncException e) {
            log.error("[GameSyncException]- {}", e.getMessage());
        }
    }
}
