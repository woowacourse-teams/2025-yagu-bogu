package com.yagubogu.sse;

import com.yagubogu.checkin.service.CheckInService;
import com.yagubogu.sse.dto.GameWithFanRateParam;
import com.yagubogu.sse.dto.event.CheckInCreatedEvent;
import com.yagubogu.sse.repository.SseEmitterRegistry;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@RequiredArgsConstructor
@Component
public class SseEventHandler {

    private final SseEmitterRegistry sseEmitterRegistry;
    private final CheckInService checkInService;

    @Async
    @TransactionalEventListener
    public void onCheckInCreated(final CheckInCreatedEvent event) {
        long startTime = System.currentTimeMillis();

        long buildStart = System.currentTimeMillis();
        List<GameWithFanRateParam> eventData = checkInService.buildCheckInEventData(LocalDate.now());
        long buildTime = System.currentTimeMillis() - buildStart;

        int targetCount = sseEmitterRegistry.all().size();

        sseEmitterRegistry.all().forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("check-in-created")
                        .data(eventData));
            } catch (IOException e) {
                sseEmitterRegistry.removeWithError(emitter, e);
            }
        });

        long totalTime = System.currentTimeMillis() - startTime;
        long sendTime = totalTime - buildTime;
        log.info("[SSE] Fan-out 완료: 총 {}ms (빌드 {}ms, 전송 {}ms), 대상: {}명",
                totalTime, buildTime, sendTime, targetCount);
    }
}
