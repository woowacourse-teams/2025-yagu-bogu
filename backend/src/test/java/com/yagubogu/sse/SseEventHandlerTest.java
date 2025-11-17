package com.yagubogu.sse;

import com.yagubogu.checkin.service.CheckInService;
import com.yagubogu.sse.dto.event.CheckInCreatedEvent;
import com.yagubogu.sse.repository.SseEmitterRegistry;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.Executor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SseEventHandlerTest {

    private SseEmitterRegistry repository;
    private CheckInService checkInService;
    private Executor sseBroadcastExecutor;
    private SseEventHandler handler;

    @BeforeEach
    void setUp() {
        repository = new SseEmitterRegistry();
        checkInService = mock(CheckInService.class);

        // 2. 'sseBroadcastExecutor'를 동기식으로 설정 (테스트 용이성)
        sseBroadcastExecutor = Runnable::run;
        handler = new SseEventHandler(repository, checkInService, sseBroadcastExecutor);

        // checkInService의 기본 Mock 동작 설정
        when(checkInService.buildCheckInEventData(any(LocalDate.class)))
                .thenReturn(List.of());
    }

    @DisplayName("플래그가 false(변경 없음)이면 스케줄러가 DB조회나 전송을 하지 않는다")
    @Test
    void broadcastLatestDataIfDirty_shouldDoNothing_whenFlagIsFalse() {
        // given
        RecordingEmitter e1 = new RecordingEmitter();
        repository.add(e1);

        // when
        // onCheckInCreated()를 호출하지 않음 (isDirty=false 상태)
        handler.broadcastLatestDataIfDirty();

        // then
        verify(checkInService, never()).buildCheckInEventData(any(LocalDate.class));
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(e1.sendCount).isEqualTo(0);
        });
    }

    @DisplayName("플래그가 true(변경 있음)이면 스케줄러가 DB조회 후 모든 Emitter에 전송한다")
    @Test
    void broadcastLatestDataIfDirty_shouldSendEvents_whenFlagIsTrue() {
        // given
        LocalDate date = LocalDate.now();
        RecordingEmitter e1 = new RecordingEmitter();
        RecordingEmitter e2 = new RecordingEmitter();
        repository.add(e1);
        repository.add(e2);

        // when
        handler.onCheckInCreated(new CheckInCreatedEvent());
        handler.broadcastLatestDataIfDirty();

        // then
        ArgumentCaptor<LocalDate> captor = ArgumentCaptor.forClass(LocalDate.class);
        verify(checkInService).buildCheckInEventData(captor.capture());
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(captor.getValue()).isEqualTo(date);
            softAssertions.assertThat(e1.sendCount).isEqualTo(1);
            softAssertions.assertThat(e2.sendCount).isEqualTo(1);
        });
    }

    @DisplayName("플래그가 true여도 전송 후 플래그가 false로 초기화된다")
    @Test
    void broadcastLatestDataIfDirty_shouldClearFlag_afterRunning() {
        // given
        RecordingEmitter e1 = new RecordingEmitter();
        repository.add(e1);

        // when
        // 1. 이벤트 발생 (isDirty = true)
        handler.onCheckInCreated(new CheckInCreatedEvent());

        // 2. 첫 번째 스케줄 실행 (isDirty가 false로 변경되어야 함)
        handler.broadcastLatestDataIfDirty();

        // 3. 두 번째 스케줄 실행 (isDirty가 false이므로 아무일도 없어야 함)
        handler.broadcastLatestDataIfDirty();

        // then
        // DB 조회와 전송은 "단 1번만" 실행되어야 함
        verify(checkInService, times(1)).buildCheckInEventData(any(LocalDate.class));
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(e1.sendCount).isEqualTo(1);
        });
    }

    static class RecordingEmitter extends SseEmitter {

        int sendCount = 0;

        RecordingEmitter() {
            super(5_000L);
        }

        @Override
        public void send(Object obj) throws IOException {
            sendCount++;
        }

        @Override
        public void send(SseEventBuilder builder) throws IOException {
            sendCount++;
        }
    }
}
