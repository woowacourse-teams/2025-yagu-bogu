package com.yagubogu.stat.schedule;

import com.yagubogu.stat.service.StatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CacheSyncScheduler {

    private final StatService statsService;

    @Scheduled(cron = "0 0 * * * *")
    public void syncCacheFromDb() {
        log.info("Hourly stats cache sync started");
        try {
            statsService.refreshCacheFromDb();
            log.info("Hourly stats cache sync completed");
        } catch (Exception e) {
            log.error("Hourly stats cache sync failed", e);
        }
    }
}
