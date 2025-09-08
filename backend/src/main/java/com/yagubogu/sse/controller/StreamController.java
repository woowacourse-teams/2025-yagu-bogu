package com.yagubogu.sse.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequiredArgsConstructor
@RequestMapping("/api/event-stream")
public class StreamController {


    @GetMapping
    public ResponseEntity<Void> getEventStream() {

    }
}
