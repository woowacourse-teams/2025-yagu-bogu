package com.yagubogu.sse;

import static org.springframework.web.servlet.mvc.method.annotation.SseEmitter.event;

import com.yagubogu.checkin.cache.FanRateCache;
import com.yagubogu.checkin.service.CheckInService;
import com.yagubogu.sse.dto.GameWithFanRateParam;
import com.yagubogu.sse.dto.event.CheckInCreatedEvent;
import com.yagubogu.sse.repository.SseEmitterRegistry;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
@Component
public class SseEventHandler {

    private final CheckInService checkInService;
    private final SseEmitterRegistry sseEmitterRegistry;
    private final FanRateCache fanRateCache;

    @Qualifier("sseExecutor")
    private final Executor sseExecutor;

    @Async
    @TransactionalEventListener
    public void onCheckInCreated(final CheckInCreatedEvent event) {
        List<GameWithFanRateParam> payload = fanRateCache.getOrCompute(
                LocalDate.now(),
                checkInService::buildCheckInEventData
        );

        List<CompletableFuture<Void>> futures = sseEmitterRegistry.all().stream()
                .map(emitter -> CompletableFuture.runAsync(() -> {
                    try {
                        emitter.send(event()
                                .name("check-in-created")
                                .data(payload));
                    } catch (IOException e) {
                        sseEmitterRegistry.removeWithError(emitter, e);
                    }
                }, sseExecutor))
                .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }
}
