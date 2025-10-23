package yagubogu.crawling.game.service.poller;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GlobalBackOffManager {

    private static final int MAX_BACKOFF_MINUTES = 8;

    private final Clock clock;
    private volatile Instant backoffUntil = Instant.EPOCH;

    public GlobalBackOffManager(Clock clock) {
        this.clock = clock;
    }

    /**
     * 전역 백오프 적용 (외부 API 장애 시)
     *
     * 지수 백오프: 1분 → 2분 → 4분 → 8분 (최대)
     * 목적: API 차단 방지, 과도한 재시도 방지
     */
    public void applyBackoff() {
        Instant now = Instant.now(clock);
        long currentRemainMinutes = Duration.between(now, backoffUntil).toMinutes();

        long nextBackoffMinutes = calculateNextBackoff(currentRemainMinutes);
        backoffUntil = now.plus(Duration.ofMinutes(nextBackoffMinutes));

        log.warn("[GLOBAL_BACKOFF] API failure detected. Backing off {} minutes until {}",
                nextBackoffMinutes, backoffUntil.atZone(clock.getZone()));
    }

    public void clear() {
        backoffUntil = Instant.EPOCH;
    }

    public boolean isActive(Instant now) {
        return now.isBefore(backoffUntil);
    }

    private long calculateNextBackoff(long currentRemainMinutes) {
        if (currentRemainMinutes <= 0) {
            return 1;  // 첫 실패: 1분
        }
        return Math.min(MAX_BACKOFF_MINUTES, currentRemainMinutes * 2);
    }
}
