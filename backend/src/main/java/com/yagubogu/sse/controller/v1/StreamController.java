package com.yagubogu.sse.controller.v1;

import com.yagubogu.auth.annotation.RequireRole;
import com.yagubogu.sse.service.SseEmitterService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RequiredArgsConstructor
@RequestMapping("/event-stream")
@RestController
public class StreamController {

    private final SseEmitterService sseEmitterService;

    @RequireRole
    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter getEventStream(HttpServletResponse response) {
        // 캐싱 방지
        response.setHeader("Cache-Control", "no-cache");
        // 연결 유지
        response.setHeader("Connection", "keep-alive");
        // nginx에서 버퍼링 방지
        response.setHeader("X-Accel-Buffering", "no");

        return sseEmitterService.add();
    }
}
