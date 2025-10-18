package com.yagubogu.talk.controller;

import com.yagubogu.auth.annotation.RequireRole;
import com.yagubogu.auth.dto.MemberClaims;
import com.yagubogu.talk.dto.TalkCursorResult;
import com.yagubogu.talk.dto.TalkEntranceResponse;
import com.yagubogu.talk.dto.TalkRequest;
import com.yagubogu.talk.dto.TalkResponse;
import com.yagubogu.talk.service.TalkReportService;
import com.yagubogu.talk.service.TalkService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequireRole
@RestController
public class TalkController implements TalkControllerInterface {

    private final TalkService talkService;
    private final TalkReportService talkReportService;

    public ResponseEntity<TalkEntranceResponse> findInitialTalks(
            final MemberClaims memberClaims,
            @PathVariable final long gameId
    ) {
        TalkEntranceResponse response = talkService.findInitialTalksExcludingReported(gameId, memberClaims.id());

        return ResponseEntity.ok(response);
    }

    public ResponseEntity<TalkCursorResult> findTalks(
            final MemberClaims memberClaims,
            @PathVariable final long gameId,
            @RequestParam(value = "before", required = false) final Long cursorId,
            @RequestParam("limit") final int limit
    ) {
        TalkCursorResult response = talkService.findTalksExcludingReported(gameId, cursorId, limit, memberClaims.id());

        return ResponseEntity.ok(response);
    }

    public ResponseEntity<TalkCursorResult> findNewTalks(
            final MemberClaims memberClaims,
            @PathVariable final long gameId,
            @RequestParam(value = "after") final long cursorId,
            @RequestParam("limit") final int limit
    ) {
        TalkCursorResult response = talkService.findNewTalks(gameId, cursorId, memberClaims.id(), limit);

        return ResponseEntity.ok(response);
    }

    public ResponseEntity<TalkResponse> createTalk(
            final MemberClaims memberClaims,
            @PathVariable final long gameId,
            @Valid @RequestBody final TalkRequest request
    ) {
        TalkResponse response = talkService.createTalk(gameId, request, memberClaims.id());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    public ResponseEntity<Void> reportTalk(
            final MemberClaims memberClaims,
            @PathVariable final long talkId
    ) {
        talkReportService.reportTalk(talkId, memberClaims.id());

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    public ResponseEntity<Void> removeTalk(
            final MemberClaims memberClaims,
            @PathVariable final long gameId,
            @PathVariable final long talkId
    ) {
        talkService.removeTalk(gameId, talkId, memberClaims.id());

        return ResponseEntity.noContent().build();
    }
}
