package yagubogu.crawling.game.scheduler;

import com.yagubogu.game.exception.GameSyncException;
import java.time.Clock;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import yagubogu.crawling.game.service.crawler.KboGameCenterCrawler.GameCenterSyncService;
import yagubogu.crawling.game.service.crawler.KboScoardboardCrawler.KboScoreboardService;
import yagubogu.crawling.game.service.poller.AdaptivePoller;

@Slf4j
@RequiredArgsConstructor
@Component
public class GameScheduler {

    private final KboScoreboardService kboScoreboardService;
    private final GameCenterSyncService gameCenterSyncService;
    private final AdaptivePoller adaptivePoller;
    private final Clock clock;

    @EventListener(ApplicationReadyEvent.class)
    @Scheduled(cron = "0 0 0 * * *")
    public void fetchDailyGameSchedule() {
        log.info("FETCH DAILY GAME SCHEDULE");
        LocalDate today = LocalDate.now(clock);
        LocalDate yesterday = today.minusDays(1);
        try {
            kboScoreboardService.fetchScoreboardRange(yesterday, today);
            gameCenterSyncService.fetchGameCenter(today);
            adaptivePoller.initializeTodaySchedule(today);
            log.info("[DAILY_SCHEDULE] Successfully completed for {}", today);
        } catch (GameSyncException e) {
            log.error("[DAILY_SCHEDULE] Failed for {}: {}", today, e.getMessage(), e);
        } catch (Exception e) {
            log.error("[DAILY_SCHEDULE] Unexpected error for {}: {}", today, e.getMessage(), e);
        }
    }
}
