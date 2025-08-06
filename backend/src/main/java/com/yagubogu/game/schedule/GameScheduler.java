package com.yagubogu.game.schedule;

import com.yagubogu.game.exception.GameSyncException;
import com.yagubogu.game.service.GameResultSyncService;
import com.yagubogu.game.service.GameSyncService;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class GameScheduler {

    private final GameSyncService gameSyncService;
    private final GameResultSyncService gameResultSyncService;

    @Scheduled(cron = "0 0 0 * * *")
    public void fetchDailyGameSchedule() {
        LocalDate today = LocalDate.now();
        try {
            gameSyncService.syncGameSchedule(today);
        } catch (GameSyncException e) {
            // TODO: 예외 로깅
        }
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void fetchDailyGameResult() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        try {
            gameResultSyncService.syncGameResult(yesterday);
        } catch (GameSyncException e) {
            // TODO: 예외 로깅
        }
    }
}
