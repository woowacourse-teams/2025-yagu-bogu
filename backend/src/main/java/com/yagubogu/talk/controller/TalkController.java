package com.yagubogu.talk.controller;

import com.yagubogu.talk.dto.CursorResult;
import com.yagubogu.talk.dto.TalkRequest;
import com.yagubogu.talk.dto.TalkResponse;
import com.yagubogu.talk.service.TalkService;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/talks")
@RestController
public class TalkController {

    private final TalkService talkService;

    @GetMapping("/{gameId}")
    public ResponseEntity<CursorResult<TalkResponse>> findTalks(
            @PathVariable final long gameId,
            @RequestParam(value = "before", required = false) final Long cursorId,
            @RequestParam("limit") final int limit
    ) {
        CursorResult<TalkResponse> response = talkService.findTalks(gameId, cursorId, limit);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{gameId}/polling")
    public ResponseEntity<CursorResult<TalkResponse>> pollTalks(
            @PathVariable final long gameId,
            @RequestParam(value = "after", required = false) final Long cursorId,
            @RequestParam("limit") final int limit
    ) {
        CursorResult<TalkResponse> response = talkService.pollTalks(gameId, cursorId, limit);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{gameId}")
    public ResponseEntity<TalkResponse> createTalk(
            @PathVariable final long gameId,
            @Valid @RequestBody final TalkRequest request
    ) {
        TalkResponse response = talkService.createTalk(gameId, request);

        URI location = URI.create("/api/talks/" + response.id());
        return ResponseEntity.created(location).body(response);
    }
}
