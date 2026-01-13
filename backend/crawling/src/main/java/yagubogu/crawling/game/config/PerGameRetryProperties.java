package yagubogu.crawling.game.config;

import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@ConfigurationProperties(prefix = "crawler.retry.per-game")
public class PerGameRetryProperties {

    /** 빠른 복구 구간: 1~N회 실패 */
    private int quickThreshold = 5;

    /** 빠른 복구 간격 */
    private Duration quickInterval = Duration.ofMinutes(2);

    /** 지수 백오프 최대(1→2→4→8분 cap) */
    private Duration maxBackoff = Duration.ofMinutes(8);

    /** 총 시도 상한(넘으면 포기 신호: null) */
    private int maxAttempts = 12;

    /** 과도 실패 경고 임계치 */
    private int warnThreshold = 15;
}
