package com.yagubogu.sse;

import com.yagubogu.checkin.service.CheckInService;
import com.yagubogu.sse.dto.GameWithFanRateParam;
import com.yagubogu.sse.dto.event.CheckInCreatedEvent;
import com.yagubogu.sse.repository.SseEmitterRegistry;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@RequiredArgsConstructor
@Component
public class SseEventHandler {

    private final AtomicBoolean isDirty = new AtomicBoolean(false);

    private final SseEmitterRegistry sseEmitterRegistry;
    private final CheckInService checkInService;
    @Qualifier("sseBroadcastExecutor")
    private final Executor sseBroadcastExecutor;

    /**
     * [이벤트 리스너] DB 조회를 하지 않고 "플래그"만 true로 설정하고 즉시 종료.
     */
    @TransactionalEventListener
    public void onCheckInCreated(final CheckInCreatedEvent event) {
        isDirty.set(true);
    }

    /**
     * [스케줄러] 0.25초(250ms)마다 "변경 플래그(isDirty)"를 확인. 플래그가 true일 경우에만 DB 조회 1번, 전체 방송 1번을 수행.
     */
    @Scheduled(fixedDelay = 250) // 0.25초 간격으로 실행
    public void broadcastLatestDataIfDirty() {
        // 'isDirty'가 true일 때만 실행 (compareAndSet으로 동시성 처리)
        if (isDirty.compareAndSet(true, false)) {
            log.info("[SSE-Scheduler] 전송 시작");
            Collection<SseEmitter> allEmitters = sseEmitterRegistry.all();
            int emitterCount = allEmitters.size();

            if (emitterCount == 0) {
                log.info("[SSE-Scheduler] 연결된 클라이언트가 없어 전송 취소");
                return;
            }

            List<SseEmitter> emitterList = new ArrayList<>(allEmitters);
            Collections.shuffle(emitterList);

            // 1. DB 조회 (1번 발생)
            List<GameWithFanRateParam> eventData = checkInService.buildCheckInEventData(LocalDate.now());

            // 2. 전체 방송 (Fan-Out)
            try {
                List<CompletableFuture<Void>> futures = emitterList.stream()
                        .map(emitter -> CompletableFuture.runAsync(() -> {
                            try {
                                emitter.send(SseEmitter.event()
                                        .name("check-in-created")
                                        .data(eventData));
                            } catch (IOException e) {
                                log.info("[SSE-Client] I/O 예외 발생, 메시지: {}", e.getMessage());
                                sseEmitterRegistry.removeWithError(emitter, e);
                            }
                        }, sseBroadcastExecutor))
                        .toList();

                CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

                allFutures.get(200, TimeUnit.MILLISECONDS);
                log.info("[SSE-Scheduler] 전송 완료 ({}명)", emitterCount);
            } catch (RejectedExecutionException e) {
                isDirty.set(true);
                log.warn("[SSE-Reject] 스레드 풀 자원 부족으로 일부 전송 스킵 - 다음 주기 재전송 예약");
            } catch (TimeoutException e) {
                isDirty.set(true);
                log.warn("[SSE-Timeout] 200ms 내 전송 미완료 - 다음 주기 재전송 예약");
            } catch (Exception e) {
                isDirty.set(true);
                log.error("[SSE-Error] 전송 중 예기치 않은 오류 발생", e);
            }
        }
    }
}
