package com.yagubogu.talk.controller;

import com.yagubogu.auth.dto.MemberClaims;
import com.yagubogu.talk.dto.TalkCursorResult;
import com.yagubogu.talk.dto.TalkRequest;
import com.yagubogu.talk.dto.TalkResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Talk", description = "경기별 톡(Talk) 관련 API")
@RequestMapping("/api/talks")
public interface TalkControllerInterface {

    @Operation(summary = "톡 조회", description = "지정한 경기에서 과거 톡 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "톡 조회 성공"),
            @ApiResponse(responseCode = "404", description = "경기 또는 회원을 찾을 수 없음")
    })
    @GetMapping("/{gameId}")
    ResponseEntity<TalkCursorResult> findTalks(
            @Parameter(hidden = true) MemberClaims memberClaims,
            @PathVariable long gameId,
            @RequestParam(value = "before", required = false) Long cursorId,
            @RequestParam("limit") int limit
    );

    @Operation(summary = "최신 톡 조회", description = "지정한 경기에서 cursor 이후의 최신 톡을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "최신 톡 조회 성공"),
            @ApiResponse(responseCode = "404", description = "경기 또는 회원을 찾을 수 없음")
    })
    @GetMapping("/{gameId}/latest")
    ResponseEntity<TalkCursorResult> findNewTalks(
            @Parameter(hidden = true) MemberClaims memberClaims,
            @PathVariable long gameId,
            @RequestParam("after") long cursorId,
            @RequestParam("limit") int limit
    );

    @Operation(summary = "톡 생성", description = "지정한 경기에서 새로운 톡을 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "톡 생성 성공"),
            @ApiResponse(responseCode = "400", description = "요청 본문이 잘못되었거나 본인 신고 등"),
            @ApiResponse(responseCode = "403", description = "차단된 회원"),
            @ApiResponse(responseCode = "404", description = "경기 또는 회원을 찾을 수 없음")
    })
    @PostMapping("/{gameId}")
    ResponseEntity<TalkResponse> createTalk(
            @Parameter(hidden = true) MemberClaims memberClaims,
            @PathVariable long gameId,
            @RequestBody @Valid TalkRequest request
    );

    @Operation(summary = "톡 신고", description = "특정 톡을 신고합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "신고 성공"),
            @ApiResponse(responseCode = "400", description = "자기 자신의 톡 신고 또는 중복 신고"),
            @ApiResponse(responseCode = "404", description = "톡 또는 회원을 찾을 수 없음")
    })
    @PostMapping("/{talkId}/reports")
    ResponseEntity<Void> reportTalk(
            @Parameter(hidden = true) MemberClaims memberClaims,
            @PathVariable long talkId
    );

    @Operation(summary = "톡 삭제", description = "자신이 작성한 톡을 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "톡 삭제 성공"),
            @ApiResponse(responseCode = "400", description = "gameId가 일치하지 않음"),
            @ApiResponse(responseCode = "403", description = "자신의 톡이 아님"),
            @ApiResponse(responseCode = "404", description = "톡 또는 경기를 찾을 수 없음")
    })
    @DeleteMapping("/{gameId}/{talkId}")
    ResponseEntity<Void> removeTalk(
            @Parameter(hidden = true) MemberClaims memberClaims,
            @PathVariable long gameId,
            @PathVariable long talkId
    );
}
