package yagubogu.crawling.game.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import yagubogu.crawling.game.service.crawler.KboGameCenterCrawler.KboGameCenterCrawler;
import yagubogu.crawling.game.service.crawler.KboScheduleCrawler.KboSchedulerCrawler;
import yagubogu.crawling.game.service.crawler.KboScoardboardCrawler.KboScoreboardCrawler;

@Configuration
@EnableConfigurationProperties(KboCrawlerProperties.class)
public class KboCrawlerConfig {

    @Bean
    public KboSchedulerCrawler kboScheduleCrawler(final KboCrawlerProperties properties) {
        return new KboSchedulerCrawler(
                properties.getNavigationTimeout(),
                properties.getTableTimeout(),
                properties.getWaitTimeout(),
                properties.getMaxRetries(),
                properties.getRetryDelay());
    }

    @Bean
    public PlaywrightManager playwrightManager() {
        return new PlaywrightManager();
    }

    @Bean
    public KboScoreboardCrawler kboScoreboardCrawler(
            final KboCrawlerProperties p,
            final PlaywrightManager playwrightManager) {
        return new KboScoreboardCrawler(
                p.getBaseUrl() + p.getScheduleUrl(),
                p.getNavigationTimeout(),
                p.getWaitTimeout(),
                playwrightManager
        );
    }

    @Bean
    public KboGameCenterCrawler kboGameCenterCrawler(
            final KboCrawlerProperties p,
            final PlaywrightManager playwrightManager) {
        return new KboGameCenterCrawler(
                p.getBaseUrl() + p.getGameCenterUrl(),
                p.getNavigationTimeout(),
                playwrightManager
        );
    }
}
