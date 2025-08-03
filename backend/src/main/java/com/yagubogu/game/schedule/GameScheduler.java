package com.yagubogu.game.schedule;

import com.yagubogu.game.service.GameSyncService;
import com.yagubogu.global.exception.ClientException;
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

        try {
            gameSyncService.syncGameSchedule(today);
        } catch (ClientException e) {
            // TODO: 예외 로깅
        }
    }
}
