package com.yagubogu.talk.service;

import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class InMemoryRateLimiter implements RateLimiter {

    private final ConcurrentHashMap<String, Queue<Long>> requestTimestamps = new ConcurrentHashMap<>();

    @Override
    public void checkLimit(String key, int maxRequests, int windowSeconds) {
        long now = System.currentTimeMillis();
        long windowStart = now - (windowSeconds * 1000L);

        Queue<Long> timestamps = requestTimestamps.computeIfAbsent(
                key,
                k -> new ConcurrentLinkedQueue<>()
        );

        // Sliding Window: 오래된 요청 제거
        synchronized (timestamps) {
            timestamps.removeIf(timestamp -> timestamp < windowStart);

            // 제한 확인
            if (timestamps.size() >= maxRequests) {
                log.warn("Rate limit exceeded - key: {}, count: {}", key, timestamps.size());
                throw new RateLimitExceededException(
                        String.format("메시지를 너무 빠르게 보내고 있습니다. (최대: %d개/%d초)", maxRequests, windowSeconds)
                );
            }

            // 현재 요청 기록
            timestamps.offer(now);
        }
    }

    // 메모리 정리 (1분마다)
    @Scheduled(fixedRate = 60000)
    public void cleanup() {
        long now = System.currentTimeMillis();
        long threshold = now - 10000; // 10초 이상 지난 데이터

        requestTimestamps.entrySet().removeIf(entry -> {
            Queue<Long> timestamps = entry.getValue();
            synchronized (timestamps) {
                timestamps.removeIf(timestamp -> timestamp < threshold);
                return timestamps.isEmpty();
            }
        });

        log.debug("Rate limiter cleanup completed. Active keys: {}", requestTimestamps.size());
    }
}
