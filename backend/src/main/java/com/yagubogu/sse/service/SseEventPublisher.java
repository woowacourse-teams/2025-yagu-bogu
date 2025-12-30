package com.yagubogu.sse.service;

import com.yagubogu.sse.repository.SseEmitterRegistry;
import com.yagubogu.sse.dto.GameWithFanRateParam;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@RequiredArgsConstructor
@Service
public class SseEventPublisher {

    private final SseEmitterRegistry sseEmitterRegistry;

    @Qualifier("statsSseExecutor")
    private final Executor statsSseExecutor;

    @Value("${sse.stats.chunk-size:100}")
    private int chunkSize;

    public void publishFanRateUpdate(final List<GameWithFanRateParam> payload) {
        List<SseEmitter> emitters = new ArrayList<>(sseEmitterRegistry.all());
        if (emitters.isEmpty()) {
            return;
        }

        long start = System.currentTimeMillis();
        AtomicInteger failed = new AtomicInteger(0);

        List<List<SseEmitter>> chunks = partition(emitters, chunkSize);
        List<CompletableFuture<Void>> futures = chunks.stream()
                .map(chunk -> CompletableFuture.runAsync(() -> sendChunk(chunk, payload, failed), statsSseExecutor))
                .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        long elapsed = System.currentTimeMillis() - start;

        log.info("Fan rate SSE sent: emitters={} failed={} elapsedMs={}",
                emitters.size(), failed.get(), elapsed);
    }

    private void sendChunk(
            final List<SseEmitter> emitters,
            final List<GameWithFanRateParam> payload,
            final AtomicInteger failed
    ) {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("check-in-created")
                        .data(payload));
            } catch (IOException e) {
                failed.incrementAndGet();
                sseEmitterRegistry.removeWithError(emitter, e);
            }
        }
    }

    private List<List<SseEmitter>> partition(final List<SseEmitter> emitters, final int size) {
        List<List<SseEmitter>> partitions = new ArrayList<>();
        for (int i = 0; i < emitters.size(); i += size) {
            int end = Math.min(i + size, emitters.size());
            partitions.add(new ArrayList<>(emitters.subList(i, end)));
        }
        return partitions;
    }
}
