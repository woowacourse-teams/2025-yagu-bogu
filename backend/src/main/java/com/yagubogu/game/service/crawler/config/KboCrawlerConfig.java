package com.yagubogu.game.service.crawler.config;

import com.yagubogu.game.service.crawler.KboScheduleCrawler.KboScheduleCrawler;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(KboCrawlerProperties.class)
public class KboCrawlerConfig {

    @Bean
    public KboScheduleCrawler kboScheduleCrawler(final KboCrawlerProperties properties) {
        return new KboScheduleCrawler(
                properties.getNavigationTimeout(),
                properties.getTableTimeout(),
                properties.getMaxRetries(),
                properties.getRetryDelay()
        );
    }
}

