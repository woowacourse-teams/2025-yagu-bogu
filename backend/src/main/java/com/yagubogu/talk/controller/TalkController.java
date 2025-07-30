package com.yagubogu.talk.controller;

import com.yagubogu.talk.dto.CursorResult;
import com.yagubogu.talk.dto.TalkResponse;
import com.yagubogu.talk.service.TalkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
            @PathVariable Long gameId,
            @RequestParam(value = "after", required = false) final Long cursorId,
            @RequestParam("limit") final int limit
    ) {
        CursorResult<TalkResponse> response = talkService.findTalks(gameId, cursorId, limit);

        return ResponseEntity.ok(response);
    }
}
