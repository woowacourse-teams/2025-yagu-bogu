package yagubogu.crawling.game.config;

import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@ConfigurationProperties(prefix = "crawler.retry.global")
public class GlobalBackoffProperties {

    /** 연속 실패 1회 때 */
    private Duration first = Duration.ofMinutes(1);

    /** 연속 실패 2~3회 때 */
    private Duration second = Duration.ofMinutes(2);

    /** 연속 실패 4회 이상 cap */
    private Duration max = Duration.ofMinutes(5);
}
