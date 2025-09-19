package com.yagubogu.sse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yagubogu.checkin.repository.CheckInRepository;
import com.yagubogu.sse.dto.CheckInCreatedEvent;
import com.yagubogu.sse.repository.SseEmitterRegistry;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

class EventHandlerTest {

    private SseEmitterRegistry repository;
    private CheckInRepository checkInRepository;
    private EventHandler handler;

    @BeforeEach
    void setUp() {
        repository = new SseEmitterRegistry();
        checkInRepository = mock(CheckInRepository.class);
        handler = new EventHandler(repository, checkInRepository);
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

    @DisplayName("특정 날짜로 이벤트가 들어오면 Repository의 모든 Emitter에 이벤트가 전달된다")
    @Test
    void onCheckInCreated_shouldSendEventToAllEmitters() {
        // given
        LocalDate date = LocalDate.of(2025, 7, 25);
        when(checkInRepository.findGamesWithFanCountsByDate(any()))
                .thenReturn(List.of());

        RecordingEmitter e1 = new RecordingEmitter();
        RecordingEmitter e2 = new RecordingEmitter();
        repository.add(e1);
        repository.add(e2);

        // when
        handler.onCheckInCreated(new CheckInCreatedEvent(date));

        // then
        ArgumentCaptor<LocalDate> captor = ArgumentCaptor.forClass(LocalDate.class);
        verify(checkInRepository).findGamesWithFanCountsByDate(captor.capture());
        assertThat(captor.getValue()).isEqualTo(date);
        assertThat(e1.sendCount).isEqualTo(1);
        assertThat(e2.sendCount).isEqualTo(1);
    }
}
