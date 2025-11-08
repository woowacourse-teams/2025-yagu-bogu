package com.yagubogu.checkin;

import static com.yagubogu.sse.SseKpis.EVENTS;
import static com.yagubogu.sse.SseKpis.QUERIES;
import static com.yagubogu.sse.SseKpis.SEND_OK;
import static com.yagubogu.sse.SseKpis.logQuery;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.yagubogu.checkin.service.CheckInService;
import com.yagubogu.sse.SseKpis;
import com.yagubogu.sse.dto.GameWithFanRateParam;
import com.yagubogu.sse.dto.event.CheckInCreatedEvent;
import com.yagubogu.sse.repository.SseEmitterRegistry;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Service
@RequiredArgsConstructor
public class CheckInEventListener {

    private final CheckInService checkInService;
    private final SseEmitterRegistry sseEmitterRegistry;

    private final Cache<LocalDate, List<GameWithFanRateParam>> eventCache =
            Caffeine.newBuilder()
                    .expireAfterWrite(2, TimeUnit.SECONDS)
                    .recordStats()
                    .build();

    @Async
    @TransactionalEventListener
    public void onCheckInCreated(final CheckInCreatedEvent event) {
        long ev = EVENTS.incrementAndGet();

        boolean[] cacheHit = {true};
        List<GameWithFanRateParam> payload;

        payload = eventCache.get(LocalDate.now(), k -> {
            cacheHit[0] = false;
            long q = QUERIES.incrementAndGet();
            logQuery(q);
            return checkInService.buildCheckInEventData(k);
        });

        if (payload == null) {
            payload = checkInService.buildCheckInEventData(LocalDate.now());
        }

        long sendOk = 0;
        for (var emitter : sseEmitterRegistry.all()) {
            try {
                emitter.send(org.springframework.web.servlet.mvc.method.annotation.SseEmitter.event()
                        .name("check-in-created")
                        .data(payload));
                sendOk++;
            } catch (IOException e) {
                sseEmitterRegistry.removeWithError(emitter, e);
            }
        }
        SEND_OK.addAndGet(sendOk);

        SseKpis.logEvent(ev, cacheHit[0], sseEmitterRegistry.size(), sendOk);
    }
}
