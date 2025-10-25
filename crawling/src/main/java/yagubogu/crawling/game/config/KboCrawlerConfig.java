package yagubogu.crawling.game.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import yagubogu.crawling.game.config.KboCrawlerProperties.CrawlerConfig;
import yagubogu.crawling.game.service.crawler.KboGameCenterCrawler.KboGameCenterCrawler;
import yagubogu.crawling.game.service.crawler.KboScheduleCrawler.KboSchedulerCrawler;
import yagubogu.crawling.game.service.crawler.KboScoardboardCrawler.KboScoreboardCrawler;

@Configuration
@EnableConfigurationProperties({
        KboCrawlerProperties.class,
        CrawlerSchedulerProperties.class,
        PerGameRetryProperties.class,
        GlobalBackoffProperties.class
})
public class KboCrawlerConfig {

    @Bean
    public KboSchedulerCrawler kboScheduleCrawler(final KboCrawlerProperties properties) {
        CrawlerConfig crawler = properties.getCrawler();
        return new KboSchedulerCrawler(
                crawler.getNavigationTimeout(),
                crawler.getTableTimeout(),
                crawler.getWaitTimeout(),
                crawler.getMaxRetries(),
                crawler.getRetryDelay());
    }

    @Bean
    public PlaywrightManager playwrightManager() {
        return new PlaywrightManager();
    }

    @Bean
    public KboScoreboardCrawler kboScoreboardCrawler(
            final KboCrawlerProperties properties,
            final PlaywrightManager playwrightManager) {
        return new KboScoreboardCrawler(
                properties,
                playwrightManager
        );
    }

    @Bean
    public KboGameCenterCrawler kboGameCenterCrawler(
            final KboCrawlerProperties properties,
            final PlaywrightManager playwrightManager) {
        CrawlerConfig crawler = properties.getCrawler();
        return new KboGameCenterCrawler(
                crawler.getGameCenterUrl(),
                properties,
                playwrightManager
        );
    }
}
