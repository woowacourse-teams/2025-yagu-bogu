package yagubogu.crawling.game.service.poller;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import yagubogu.crawling.game.config.PerGameRetryProperties;

@Slf4j
@Component
public class BackoffStrategy {

    private final Clock clock;
    private final PerGameRetryProperties props;
    private final Map<Long, Integer> failureCount = new ConcurrentHashMap<>();

    public BackoffStrategy(Clock clock, final PerGameRetryProperties props) {
        this.clock = clock;
        this.props = props;
    }

    /**
     * 경기별 재시도 전략 적용
     *
     * 전략:
     * - 1~5회 실패: 1분 고정 (일시적 네트워크 오류 대응)
     * - 6회 실패: 게임센터 확인 필요 신호 (취소 여부 확인)
     * - 7회 이상: 지수 백오프 (1→2→4→8분, 최대 8분)
     *
     * @return 다음 재시도 시각, 6회 실패 시 null (게임센터 확인 필요)
     */
    public Instant applyGameBackoff(Long gameId) {
        int failCount = failureCount.merge(gameId, 1, Integer::sum);

        // Phase 1: 빠른 복구 시도
        if (failCount <= props.getQuickThreshold()) {
            return Instant.now(clock)
                    .plus(props.getQuickInterval());
        }

        // Phase 2: 게임센터 확인 필요 (null 반환으로 신호)
        if (failCount == props.getQuickThreshold() + 1) {
            return null;
        }

        // Phase 3: 지수 백오프
        int backoffMultiplier = Math.min(
                failCount - props.getQuickThreshold(),
                3  // 최대 2^3 = 8분
        );
        long minutes = Math.min(props.getMaxBackoff().toMinutes(), 1L << backoffMultiplier);

        if (failCount > props.getWarnThreshold()) {
            log.warn("[BACKOFF] excessive failures: gameId={}, attempts={}", gameId, failCount);
        }

        if (failCount >= props.getMaxAttempts()) {
            log.warn("[BACKOFF] give up: gameId={}, attempts={}", gameId, failCount);
            return null;
        }

        return Instant.now(clock).plus(Duration.ofMinutes(minutes));
    }

    public void resetFailureCount(Long gameId) {
        failureCount.remove(gameId);
    }

    public void clearAll() {
        failureCount.clear();
    }
}

