package com.yagubogu.talk.controller;

import com.yagubogu.talk.dto.CursorResult;
import com.yagubogu.talk.dto.TalkRequest;
import com.yagubogu.talk.dto.TalkResponse;
import com.yagubogu.talk.service.TalkReportService;
import com.yagubogu.talk.service.TalkService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
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

    private final TalkReportService talkReportService;

    @GetMapping("/{gameId}")
    public ResponseEntity<CursorResult<TalkResponse>> findTalks(
            @PathVariable final long gameId,
            @RequestParam(value = "before", required = false) final Long cursorId,
            @RequestParam("limit") final int limit,
            @RequestParam("memberId") final long memberId // TODO: 나중에 제거
    ) {
        CursorResult<TalkResponse> response = talkService.findTalksExcludingReported(gameId, cursorId,
                limit, memberId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{gameId}/polling")
    public ResponseEntity<CursorResult<TalkResponse>> pollTalks(
            @PathVariable final long gameId,
            @RequestParam(value = "after") final long cursorId,
            @RequestParam("limit") final int limit
    ) {
        CursorResult<TalkResponse> response = talkService.pollTalks(gameId, cursorId, limit);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{gameId}")
    public ResponseEntity<TalkResponse> createTalk(
            @PathVariable final long gameId,
            @Valid @RequestBody final TalkRequest request,
            @RequestParam("memberId") final long memberId // TODO: 아이디 삭제
    ) {
        TalkResponse response = talkService.createTalk(gameId, request, memberId);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{talkId}/reports")
    public ResponseEntity<Void> reportTalk(
            @PathVariable final long talkId,
            @RequestParam("reporterId") final long reporterId // TODO: 나중에 삭제
    ) {
        talkReportService.reportTalk(talkId, reporterId);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{gameId}/{talkId}")
    public ResponseEntity<Void> removeTalk(
            @PathVariable final long gameId,
            @PathVariable final long talkId,
            @RequestParam("memberId") final long memberId // TODO: 나중에 삭제
    ) {
        talkService.removeTalk(gameId, talkId, memberId);
        return ResponseEntity.noContent().build();
    }
}
