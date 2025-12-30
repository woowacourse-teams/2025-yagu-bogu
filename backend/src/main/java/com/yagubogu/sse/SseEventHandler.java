package com.yagubogu.sse;

import com.yagubogu.checkin.cache.FanRateCache;
import com.yagubogu.checkin.service.CheckInService;
import com.yagubogu.sse.dto.GameWithFanRateParam;
import com.yagubogu.sse.dto.event.CheckInCreatedEvent;
import com.yagubogu.sse.service.SseEventPublisher;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
@Component
public class SseEventHandler {

    private final CheckInService checkInService;
    private final FanRateCache fanRateCache;
    private final SseEventPublisher sseEventPublisher;

    @Async
    @TransactionalEventListener
    public void onCheckInCreated(final CheckInCreatedEvent event) {
        List<GameWithFanRateParam> payload = fanRateCache.getOrCompute(
                LocalDate.now(),
                checkInService::buildCheckInEventData
        );

        sseEventPublisher.publishFanRateUpdate(payload);
    }
}
