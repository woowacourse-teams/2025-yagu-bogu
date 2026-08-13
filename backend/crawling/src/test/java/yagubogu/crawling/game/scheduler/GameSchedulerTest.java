package yagubogu.crawling.game.scheduler;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yagubogu.game.service.GameEtlService;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import yagubogu.crawling.game.service.crawler.KboGameCenterCrawler.GameCenterSyncService;
import yagubogu.crawling.game.service.crawler.KboScoardboardCrawler.KboScoreboardService;
import yagubogu.crawling.game.service.poller.AdaptivePoller;

class GameSchedulerTest {

    @DisplayName("GameCenter 경기 정보가 바뀌면 당일 ETL 후 Poller 일정을 다시 계산한다")
    @Test
    void refreshChangedGameMetadata() {
        KboScoreboardService scoreboardService = mock(KboScoreboardService.class);
        GameCenterSyncService gameCenterSyncService = mock(GameCenterSyncService.class);
        AdaptivePoller adaptivePoller = mock(AdaptivePoller.class);
        GameEtlService gameEtlService = mock(GameEtlService.class);
        Clock clock = Clock.fixed(Instant.parse("2026-08-13T05:00:00Z"), ZoneId.of("Asia/Seoul"));
        GameScheduler scheduler = new GameScheduler(
                scoreboardService, gameCenterSyncService, adaptivePoller, gameEtlService, clock
        );
        LocalDate today = LocalDate.of(2026, 8, 13);

        when(gameCenterSyncService.fetchGameCenter(today)).thenReturn(1);
        when(gameEtlService.transformPendingDateRange(today, today)).thenReturn(1);

        scheduler.refreshTodayGameMetadata();

        verify(gameEtlService).transformPendingDateRange(today, today);
        verify(adaptivePoller).initializeTodaySchedule(today);
    }
}
