package com.yagubogu.game.schedule;

import com.yagubogu.game.domain.AdaptivePoller;
import com.yagubogu.game.exception.GameSyncException;
import com.yagubogu.game.service.GameScheduleSyncService;
import java.time.Clock;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class GameScheduler {

    private final GameScheduleSyncService gameScheduleSyncService;
    private final AdaptivePoller adaptivePoller;
    private final Clock clock;

    /**
     * 매일 0시 or 부팅시에 경기 일정을 가져온다.
     * 이미 경기가 db에 존재하면 pass, 스케줄을 항상 재구성된다.
     */
    @Scheduled(cron = "0 0 0 * * *")
    @EventListener(ApplicationReadyEvent.class)
    public void fetchDailyGameSchedule() {
        LocalDate today = LocalDate.now(clock);

        try {
            gameScheduleSyncService.fetchGameSchedule(today);
            adaptivePoller.initializeTodaySchedule(today);
        } catch (GameSyncException e) {
            log.error("[GameSyncException]- {}", e.getMessage());
        }
    }
}
