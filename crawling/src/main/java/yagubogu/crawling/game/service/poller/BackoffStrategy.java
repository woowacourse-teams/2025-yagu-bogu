package yagubogu.crawling.game.service.poller;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BackoffStrategy {

    private static final int QUICK_RETRY_THRESHOLD = 5;
    private static final int QUICK_RETRY_INTERVAL_MINUTES = 2;
    private static final int MAX_BACKOFF_MINUTES = 8;
    private static final int MAX_RETRY_WARNING_COUNT = 15;

    private final Clock clock;
    private final Map<Long, Integer> failureCount = new ConcurrentHashMap<>();

    public BackoffStrategy(Clock clock) {
        this.clock = clock;
    }

    /**
     * 경기별 재시도 전략 적용
     *
     * 전략:
     * - 1~5회 실패: 2분 고정 (일시적 네트워크 오류 대응)
     * - 6회 실패: 게임센터 확인 필요 신호 (취소 여부 확인)
     * - 7회 이상: 지수 백오프 (1→2→4→8분, 최대 8분)
     *
     * @return 다음 재시도 시각, 6회 실패 시 null (게임센터 확인 필요)
     */
    public Instant applyGameBackoff(Long gameId) {
        int failCount = failureCount.merge(gameId, 1, Integer::sum);

        // Phase 1: 빠른 복구 시도
        if (failCount <= QUICK_RETRY_THRESHOLD) {
            return Instant.now(clock)
                    .plus(Duration.ofMinutes(QUICK_RETRY_INTERVAL_MINUTES));
        }

        // Phase 2: 게임센터 확인 필요 (null 반환으로 신호)
        if (failCount == QUICK_RETRY_THRESHOLD + 1) {
            return null;
        }

        // Phase 3: 지수 백오프
        int backoffMultiplier = Math.min(
                failCount - QUICK_RETRY_THRESHOLD,
                3  // 최대 2^3 = 8분
        );
        int minutes = Math.min(MAX_BACKOFF_MINUTES, 1 << backoffMultiplier);

        warnIfExcessiveFailures(gameId, failCount);

        return Instant.now(clock).plus(Duration.ofMinutes(minutes));
    }

    public void resetFailureCount(Long gameId) {
        failureCount.remove(gameId);
    }

    public void clearAll() {
        failureCount.clear();
    }

    private void warnIfExcessiveFailures(Long gameId, int failCount) {
        if (failCount > MAX_RETRY_WARNING_COUNT) {
            log.warn("[BACKOFF] Excessive failures detected. Manual check recommended: " +
                    "gameId={}, failCount={}", gameId, failCount);
        }
    }
}

