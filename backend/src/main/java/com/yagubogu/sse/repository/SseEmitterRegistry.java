package com.yagubogu.sse.repository;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Component
public class SseEmitterRegistry {

    private final Map<String, SseEmitter> sseEmitterMap = new ConcurrentHashMap<>();

    public SseEmitter add(final SseEmitter sseEmitter) {
        String id = UUID.randomUUID().toString();
        sseEmitterMap.put(id, sseEmitter);

        Runnable cleanup = () -> sseEmitterMap.remove(id);

        sseEmitter.onTimeout(() -> {
            try {
                log.info("SSE time out");
                sseEmitter.send(SseEmitter.event()
                        .name("timeout")
                        .data("server-timeout")
                        .reconnectTime(3000));
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                cleanup.run();
            }
        });

        sseEmitter.onCompletion(() -> {
            log.info("SSE completed: emitter={}", sseEmitter.hashCode());
            cleanup.run();
        });
        sseEmitter.onError(t -> {
            if (isClientDisconnect(t)) {
                log.warn("SSE client disconnect: emitterId={}, cause={}", id, rootMessage(t));
            } else if (isAlreadyCompleted(t)) {
                log.debug("SSE already completed: emitterId={}, cause={}", id, rootMessage(t));
            } else {
                log.error("SSE send failed: emitterId={}, cause={}", id, rootMessage(t));
            }
            cleanup.run();
        });

        return sseEmitter;
    }

    public int size() {
        return sseEmitterMap.size();
    }

    public Collection<SseEmitter> all() {
        return sseEmitterMap.values();
    }

    public void removeWithError(final SseEmitter emitter, final Throwable throwable) {
        emitter.completeWithError(throwable);
        sseEmitterMap.values().removeIf(e -> e.equals(emitter));
    }

    private boolean isClientDisconnect(Throwable t) {
        String m = rootMessage(t).toLowerCase();
        return m.contains("broken pipe") || m.contains("clientabort") || m.contains("eof");
    }

    private boolean isAlreadyCompleted(Throwable t) {
        return t instanceof IllegalStateException;
    }

    private String rootMessage(Throwable t) {
        return (t.getCause() != null ? t.getCause() : t).getMessage();
    }
}
