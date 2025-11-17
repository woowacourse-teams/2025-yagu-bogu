package yagubogu.crawling.game.service.poller;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import yagubogu.crawling.game.config.GlobalBackoffProperties;

@Slf4j
@Component
public class GlobalBackOffManager {

    private final Clock clock;
    private final GlobalBackoffProperties props;
    private final AtomicInteger consecutiveFailures = new AtomicInteger(0);

    private volatile Instant until = Instant.EPOCH;

    public GlobalBackOffManager(Clock clock, final GlobalBackoffProperties props) {
        this.clock = clock;
        this.props = props;
    }

    /**
     * 전역 백오프 적용 (외부 API 장애 시)
     *
     * 지수 백오프: 1분 → 2분 → 4분 -> 8분 (최대)
     * 목적: API 차단 방지, 과도한 재시도 방지
     */
    public void applyBackoff() {
        int failCount = consecutiveFailures.incrementAndGet();
        Duration ms = backoffMs(failCount);
        until = Instant.now(clock).plus(ms);
        log.warn("[GLOBAL_BACKOFF] failures={}, backoff={}ms until {}",
                failCount, ms, until.atZone(clock.getZone()));
    }

    public void clear() {
        consecutiveFailures.set(0);
        until = Instant.EPOCH;
    }

    public boolean isActive(Instant now) {
        return now.isBefore(until);
    }

    private Duration backoffMs(int failCount) {
        if (failCount <= 1) {
            return props.getFirst();
        }
        if (failCount <= 3) {
            return props.getSecond();
        }
        return props.getMax();
    }
}
