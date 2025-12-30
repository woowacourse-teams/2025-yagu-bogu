package com.yagubogu.sse;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yagubogu.checkin.cache.FanRateCache;
import com.yagubogu.checkin.service.CheckInService;
import com.yagubogu.sse.dto.event.CheckInCreatedEvent;
import com.yagubogu.sse.dto.GameWithFanRateParam;
import com.yagubogu.sse.service.SseEventPublisher;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

class SseEventHandlerTest {

    private CheckInService checkInService;
    private SseEventHandler handler;
    private FanRateCache fanRateCache;
    private SseEventPublisher sseEventPublisher;

    @BeforeEach
    void setUp() {
        checkInService = mock(CheckInService.class);
        fanRateCache = new FanRateCache();
        sseEventPublisher = Mockito.mock(SseEventPublisher.class);
        handler = new SseEventHandler(checkInService, fanRateCache, sseEventPublisher);
    }

    @DisplayName("특정 날짜로 이벤트가 들어오면 팬비율 payload를 조회 후 전송한다")
    @Test
    void onCheckInCreated_shouldSendPayload() {
        // given
        LocalDate date = LocalDate.now();
        List<GameWithFanRateParam> payload = List.of(mock(GameWithFanRateParam.class));
        when(checkInService.buildCheckInEventData(any()))
                .thenReturn(payload);

        // when
        handler.onCheckInCreated(new CheckInCreatedEvent());

        // then
        ArgumentCaptor<LocalDate> captor = ArgumentCaptor.forClass(LocalDate.class);
        verify(checkInService).buildCheckInEventData(captor.capture());
        verify(sseEventPublisher).publishFanRateUpdate(payload);
        assertSoftly(softAssertions ->
                softAssertions.assertThat(captor.getValue()).isEqualTo(date)
        );
    }
}
