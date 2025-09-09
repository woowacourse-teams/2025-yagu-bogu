package com.yagubogu.sse.controller;

import com.yagubogu.sse.service.SseEmitterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequiredArgsConstructor
@RequestMapping("/api/event-stream")
public class StreamController {

    private final SseEmitterService sseEmitterService;

    @GetMapping
    public ResponseEntity<Void> getEventStream() {
        sseEmitterService.add();
        return ResponseEntity.ok().build();
    }
}
