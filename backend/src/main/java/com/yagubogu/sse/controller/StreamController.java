package com.yagubogu.sse.controller;

import com.yagubogu.sse.service.SseEmitterService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RequiredArgsConstructor
@RequestMapping("/api/event-stream")
@RestController
public class StreamController {

    private final SseEmitterService sseEmitterService;

    @GetMapping
    public SseEmitter getEventStream() {
        return sseEmitterService.add();
    }
}
