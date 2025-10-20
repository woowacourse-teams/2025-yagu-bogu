package yagubogu.crawling.game.scheduler;

import com.yagubogu.game.exception.GameSyncException;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import yagubogu.crawling.game.service.crawler.KboGameCenterCrawler.GameCenterSyncService;
import yagubogu.crawling.game.service.crawler.KboScoardboardCrawler.KboScoreboardService;

@Slf4j
@RequiredArgsConstructor
@Component
public class GameScheduler {

    private final KboScoreboardService kboScoreboardService;
    private final GameCenterSyncService gameCenterSyncService;

    @Scheduled(cron = "0 0 0 * * *")
    public void fetchDailyGameSchedule() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDate today = LocalDate.now();
        try {
            kboScoreboardService.fetchScoreboardRange(yesterday, today);

            gameCenterSyncService.fetchGameCenter(today); // ??
        } catch (GameSyncException e) {
            log.error("[GameSyncException]- {}", e.getMessage());
        }
    }
}
