package com.yagubogu.stat.schedule;

import com.yagubogu.stat.service.StatService;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class StatScheduler {

    private final StatService statService;

    @Scheduled(cron = "0 0 3 * * *")
    public void updateVictoryRanking() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        try {
            statService.updateRankings(yesterday);
        } catch (RuntimeException e) {
            log.error("[{}]- {}", e.getClass().getSimpleName(), e.getMessage());
        }
    }
}
