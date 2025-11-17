package com.yagubogu.sse;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yagubogu.checkin.cache.FanRateCache;
import com.yagubogu.checkin.service.CheckInService;
import com.yagubogu.sse.dto.event.CheckInCreatedEvent;
import com.yagubogu.sse.repository.SseEmitterRegistry;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.Executor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

class SseEventHandlerTest {

    private SseEmitterRegistry sseEmitterRegistry;
    private CheckInService checkInService;
    private SseEventHandler handler;
    private FanRateCache fanRateCache;
    private Executor sseExecutor;

    @BeforeEach
    void setUp() {
        sseEmitterRegistry = new SseEmitterRegistry();
        checkInService = mock(CheckInService.class);
        fanRateCache = new FanRateCache();
        sseExecutor = Runnable::run;
        handler = new SseEventHandler(checkInService, sseEmitterRegistry, fanRateCache, sseExecutor);
    }

    @DisplayName("특정 날짜로 이벤트가 들어오면 Repository의 모든 Emitter에 이벤트가 전달된다")
    @Test
    void onCheckInCreated_shouldSendEventToAllEmitters() {
        // given
        LocalDate date = LocalDate.now();
        when(checkInService.buildCheckInEventData(any()))
                .thenReturn(List.of());

        RecordingEmitter e1 = new RecordingEmitter();
        RecordingEmitter e2 = new RecordingEmitter();
        sseEmitterRegistry.add(e1);
        sseEmitterRegistry.add(e2);

        // when
        handler.onCheckInCreated(new CheckInCreatedEvent());

        // then
        ArgumentCaptor<LocalDate> captor = ArgumentCaptor.forClass(LocalDate.class);
        verify(checkInService).buildCheckInEventData(captor.capture());
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(captor.getValue()).isEqualTo(date);
            softAssertions.assertThat(e1.sendCount).isEqualTo(1);
            softAssertions.assertThat(e2.sendCount).isEqualTo(1);
        });
    }

    static class RecordingEmitter extends SseEmitter {

        int sendCount = 0;

        RecordingEmitter() {
            super(5_000L);
        }

        @Override
        public void send(Object obj) {
            sendCount++;
        }

        @Override
        public void send(SseEventBuilder builder) {
            sendCount++;
        }
    }
}
