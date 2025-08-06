package com.yagubogu.game.schedule;

import com.yagubogu.game.exception.GameSyncException;
import com.yagubogu.game.service.GameSyncService;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class GameScheduler {

    private final GameSyncService gameSyncService;

    @Scheduled(cron = "0 0 0 * * *")
    public void fetchDailyGame() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        try {
            gameSyncService.syncGameSchedule(today);
            gameSyncService.syncGameResult(yesterday);
        } catch (GameSyncException e) {
            // TODO: 예외 로깅
        }
    }
}
