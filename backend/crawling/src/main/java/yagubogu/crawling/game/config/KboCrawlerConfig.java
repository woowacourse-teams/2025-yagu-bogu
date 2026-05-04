package yagubogu.crawling.game.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import yagubogu.crawling.game.service.crawler.KboHtmlClient;
import yagubogu.crawling.game.service.crawler.KboGameCenterCrawler.KboGameCenterCrawler;
import yagubogu.crawling.game.service.crawler.KboReviewCrawler.KboReviewCrawler;
import yagubogu.crawling.game.service.crawler.KboScoardboardCrawler.KboScoreboardCrawler;
import yagubogu.crawling.game.service.crawler.KboScoardboardCrawler.KboScoreboardParser;

@Configuration
@EnableConfigurationProperties({
        KboCrawlerProperties.class,
        CrawlerSchedulerProperties.class,
        PerGameRetryProperties.class,
        GlobalBackoffProperties.class
})
public class KboCrawlerConfig {

    @Bean
    public KboHtmlClient kboHtmlClient(final KboCrawlerProperties properties) {
        return new KboHtmlClient(properties);
    }

    @Bean
    public KboScoreboardParser kboScoreboardParser(final KboCrawlerProperties properties) {
        return new KboScoreboardParser(properties);
    }

    @Bean
    public KboScoreboardCrawler kboScoreboardCrawler(
            final KboHtmlClient kboHtmlClient,
            final KboScoreboardParser kboScoreboardParser) {
        return new KboScoreboardCrawler(
                kboHtmlClient,
                kboScoreboardParser
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
