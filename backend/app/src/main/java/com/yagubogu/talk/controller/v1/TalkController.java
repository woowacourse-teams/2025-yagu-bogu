package com.yagubogu.talk.controller.v1;

import com.yagubogu.auth.annotation.RequireRole;
import com.yagubogu.auth.dto.MemberClaims;
import com.yagubogu.talk.dto.v1.TalkCursorResultResponse;
import com.yagubogu.talk.dto.v1.TalkEntranceResponse;
import com.yagubogu.talk.dto.v1.TalkRequest;
import com.yagubogu.talk.dto.v1.TalkResponse;
import com.yagubogu.talk.service.RateLimiter;
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

    private static final int MAX_REQUESTS_PER_SECOND = 3;
    private static final int RATE_LIMIT_WINDOW_SECONDS = 1;

    private final TalkService talkService;
    private final TalkReportService talkReportService;
    private final RateLimiter rateLimiter;

    public ResponseEntity<TalkEntranceResponse> findInitialTalks(
            final MemberClaims memberClaims,
            @PathVariable final long gameId
    ) {
        TalkEntranceResponse response = talkService.findInitialTalksExcludingReported(gameId, memberClaims.id());

        return ResponseEntity.ok(response);
    }

    public ResponseEntity<TalkCursorResultResponse> findTalks(
            final MemberClaims memberClaims,
            @PathVariable final long gameId,
            @RequestParam(value = "before", required = false) final Long cursorId,
            @RequestParam("limit") final int limit
    ) {
        TalkCursorResultResponse response = talkService.findTalksExcludingReported(gameId, cursorId, limit,
                memberClaims.id());

        return ResponseEntity.ok(response);
    }

    public ResponseEntity<TalkCursorResultResponse> findNewTalks(
            final MemberClaims memberClaims,
            @PathVariable final long gameId,
            @RequestParam(value = "after") final long cursorId,
            @RequestParam("limit") final int limit
    ) {
        TalkCursorResultResponse response = talkService.findNewTalks(gameId, cursorId, memberClaims.id(), limit);

        return ResponseEntity.ok(response);
    }

    public ResponseEntity<TalkResponse> createTalk(
            final MemberClaims memberClaims,
            @PathVariable final long gameId,
            @Valid @RequestBody final TalkRequest request
    ) {
        rateLimiter.checkLimit(
                "talk:member:" + memberClaims.id(),
                MAX_REQUESTS_PER_SECOND,
                RATE_LIMIT_WINDOW_SECONDS
        );
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
