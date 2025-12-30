package yagubogu.crawling.game.config;

import java.time.Duration;
import java.time.LocalTime;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.format.annotation.DateTimeFormat;

@Getter
@Setter
@ConfigurationProperties(prefix = "crawler.scheduler")
public class CrawlerSchedulerProperties {

    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime defaultGameStartTime = LocalTime.of(18, 30);

    private Duration pollingInterval = Duration.ofMinutes(1);
}
