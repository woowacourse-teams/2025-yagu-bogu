package yagubogu.crawling.game.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import yagubogu.crawling.game.service.crawler.KboHtmlClient;
import yagubogu.crawling.game.service.crawler.KboGameCenterCrawler.KboGameCenterCrawler;
import yagubogu.crawling.game.service.crawler.KboReviewCrawler.KboReviewCrawler;
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
    public PlaywrightManager playwrightManager() {
        return new PlaywrightManager();
    }

    @Bean
    public KboHtmlClient kboHtmlClient(final KboCrawlerProperties properties) {
        return new KboHtmlClient(properties);
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
            final KboHtmlClient kboHtmlClient) {
        return new KboGameCenterCrawler(kboHtmlClient);
    }

    @Bean
    public KboReviewCrawler kboReviewCrawler(
            final KboHtmlClient kboHtmlClient) {
        return new KboReviewCrawler(kboHtmlClient);
    }
}
