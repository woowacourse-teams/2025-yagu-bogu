package com.yagubogu.sse.repository;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Component
public class SseEmitterRepository {

    private final Map<String, SseEmitter> sseEmitterMap = new ConcurrentHashMap<>();

    public SseEmitter add(final SseEmitter sseEmitter) {
        String id = UUID.randomUUID().toString();
        sseEmitterMap.put(id, sseEmitter);

        Runnable cleanup = () -> sseEmitterMap.remove(id);
        sseEmitter.onTimeout(cleanup);
        sseEmitter.onCompletion(cleanup);
        sseEmitter.onError(t -> cleanup.run());

        return sseEmitter;
    }

    public Collection<SseEmitter> all() {
        return sseEmitterMap.values();
    }
}
