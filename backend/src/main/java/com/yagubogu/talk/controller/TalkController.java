package com.yagubogu.talk.controller;

import com.yagubogu.auth.annotation.RequireRole;
import com.yagubogu.auth.dto.MemberClaims;
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
@RequireRole
@RequestMapping("/api/talks")
@RestController
public class TalkController {

    private final TalkService talkService;
    private final TalkReportService talkReportService;

    @GetMapping("/{gameId}")
    public ResponseEntity<CursorResult<TalkResponse>> findTalks(
            final MemberClaims memberClaims,
            @PathVariable final long gameId,
            @RequestParam(value = "before", required = false) final Long cursorId,
            @RequestParam("limit") final int limit
    ) {
        CursorResult<TalkResponse> response = talkService.findTalksExcludingReported(gameId, cursorId,
                limit, memberClaims.id());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{gameId}/latest")
    public ResponseEntity<CursorResult<TalkResponse>> findNewTalks(
            final MemberClaims memberClaims,
            @PathVariable final long gameId,
            @RequestParam(value = "after") final long cursorId,
            @RequestParam("limit") final int limit
    ) {
        CursorResult<TalkResponse> response = talkService.findNewTalks(gameId, cursorId, memberClaims.id(), limit);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{gameId}")
    public ResponseEntity<TalkResponse> createTalk(
            final MemberClaims memberClaims,
            @PathVariable final long gameId,
            @Valid @RequestBody final TalkRequest request
    ) {
        TalkResponse response = talkService.createTalk(gameId, request, memberClaims.id());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{talkId}/reports")
    public ResponseEntity<Void> reportTalk(
            final MemberClaims memberClaims,
            @PathVariable final long talkId
    ) {
        talkReportService.reportTalk(talkId, memberClaims.id());

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{gameId}/{talkId}")
    public ResponseEntity<Void> removeTalk(
            final MemberClaims memberClaims,
            @PathVariable final long gameId,
            @PathVariable final long talkId
    ) {
        talkService.removeTalk(gameId, talkId, memberClaims.id());

        return ResponseEntity.noContent().build();
    }
}
