package com.yagubogu.sse.service;

import com.yagubogu.sse.repository.SseEmitterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RequiredArgsConstructor
@Component
public class SseHeartbeatScheduler {

    private final SseEmitterRepository repository;

    @Scheduled(fixedRateString = "${sse.heartbeat.interval-ms}")
    public void sendHeartbeat() {
        for (SseEmitter emitter : repository.all()) {
            try {
                emitter.send(SseEmitter.event()
                        .name("ping")
                        .data(":"));
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        }
    }
}
