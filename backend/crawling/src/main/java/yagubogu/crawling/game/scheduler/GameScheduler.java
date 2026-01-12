package yagubogu.crawling.game.scheduler;

import com.yagubogu.game.exception.GameSyncException;
import com.yagubogu.game.service.GameEtlService;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private final GameEtlService gameEtlService;
    private final Clock clock;

    @EventListener(ApplicationReadyEvent.class)
    @Scheduled(cron = "0 0 0 * * *")
    public void fetchDailyGameSchedule() {
        log.info("[DAILY_SCHEDULE] Starting daily game schedule fetch");
        LocalDate today = LocalDate.now(clock);
        LocalDate yesterday = today.minusDays(1);

        try {
            // 1. 크롤링 + Bronze 저장 (동기)
            kboScoreboardService.fetchScoreboardRange(yesterday, today);
            int savedCount = gameCenterSyncService.fetchGameCenter(today);
            log.info("[DAILY_SCHEDULE] Bronze layer saved {} games", savedCount);

            // 2. 즉시 ETL 실행 (오늘 날짜)
            LocalDateTime todayStart = today.atStartOfDay();
            int etlCount = gameEtlService.transformBronzeToSilver(todayStart);
            log.info("[DAILY_SCHEDULE] ETL completed: {} games transformed", etlCount);

            // 3. AdaptivePoller 초기화 (Silver 최신 상태)
            adaptivePoller.initializeTodaySchedule(today);
            log.info("[DAILY_SCHEDULE] Successfully completed for {}", today);

        } catch (GameSyncException e) {
            log.error("[DAILY_SCHEDULE] Failed for {}: {}", today, e.getMessage(), e);
        } catch (Exception e) {
            log.error("[DAILY_SCHEDULE] Unexpected error for {}: {}", today, e.getMessage(), e);
        }
    }
}
