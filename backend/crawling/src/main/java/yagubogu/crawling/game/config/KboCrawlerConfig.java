package yagubogu.crawling.game.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import yagubogu.crawling.game.service.crawler.KboHttpClient;
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
    public KboHttpClient kboHttpClient(
            final KboCrawlerProperties properties,
            final ObjectMapper objectMapper) {
        return new KboHttpClient(properties, objectMapper);
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
            final KboHttpClient kboHttpClient) {
        return new KboGameCenterCrawler(kboHttpClient);
    }

    @Bean
    public KboReviewCrawler kboReviewCrawler(
            final KboHttpClient kboHttpClient,
            final ObjectMapper objectMapper) {
        return new KboReviewCrawler(kboHttpClient, objectMapper);
    }
}
