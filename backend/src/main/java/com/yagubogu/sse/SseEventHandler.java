package com.yagubogu.sse;

import com.yagubogu.checkin.service.CheckInService;
import com.yagubogu.sse.dto.CheckInCreatedEvent;
import com.yagubogu.sse.dto.GameWithFanRateResponse;
import com.yagubogu.sse.repository.SseEmitterRegistry;
import java.io.IOException;
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
        List<GameWithFanRateResponse> eventData = checkInService.buildCheckInEventData(event.date());

        sseEmitterRegistry.all().forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("check-in-created")
                        .data(eventData));
            } catch (IOException e) {
                System.err.println("SSE 전송 실패: " + e.getMessage());
            }
        });
    }
}
