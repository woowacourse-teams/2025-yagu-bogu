package com.yagubogu.sse.service;

import com.yagubogu.sse.repository.SseEmitterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RequiredArgsConstructor
@Component
public class SseHeartbeatScheduler {

    private final SseEmitterRegistry registry;

    @Scheduled(fixedRateString = "${sse.heartbeat.interval-ms}")
    public void sendHeartbeat() {
        for (SseEmitter emitter : registry.all()) {

            try {
                emitter.send(SseEmitter.event().comment("keepalive"));
            } catch (Exception e) {
                registry.removeWithError(emitter, e);
            }
        }
    }
}
