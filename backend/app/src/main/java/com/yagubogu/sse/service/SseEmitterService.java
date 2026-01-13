package com.yagubogu.sse.service;

import com.yagubogu.sse.repository.SseEmitterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RequiredArgsConstructor
@Service
public class SseEmitterService {

    private static final long ONE_HOUR_TIMEOUT = 60L * 60 * 1000;

    private final SseEmitterRegistry registry;

    public SseEmitter add() {
        SseEmitter emitter = new SseEmitter(ONE_HOUR_TIMEOUT);
        try {
            emitter.send(SseEmitter.event()
                    .name("connect")
                    .data("connected"));
        } catch (Exception e) {
            registry.removeWithError(emitter, e);
        }

        return registry.add(emitter);
    }
}
