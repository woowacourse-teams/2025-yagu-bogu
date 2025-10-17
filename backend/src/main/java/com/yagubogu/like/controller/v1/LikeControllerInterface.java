package com.yagubogu.like.controller.v1;

import com.yagubogu.auth.dto.MemberClaims;
import com.yagubogu.like.dto.v1.LikeBatchRequest;
import com.yagubogu.like.dto.v1.LikeCountsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Like", description = "좋아요 인증 관련 API")
@RequestMapping("/api/games")
public interface LikeControllerInterface {

    @Operation(
            summary = "좋아요 배치 전송",
            description = "특정 경기에서 발생한 좋아요 이벤트들을 배치 단위로 서버에 전송합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "배치 적용 성공"),
            @ApiResponse(responseCode = "404", description = "경기 또는 회원 정보를 찾을 수 없음")
    })
    @PostMapping("/{gameId}/like-batches")
    ResponseEntity<Void> applyLikeBatch(
            @Parameter(hidden = true) MemberClaims memberClaims,
            @PathVariable long gameId,
            @RequestBody LikeBatchRequest request
    );

    @Operation(
            summary = "좋아요 개수 조회",
            description = "특정 경기의 팀별 좋아요 개수를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "경기 정보를 찾을 수 없음")
    })
    @GetMapping("/{gameId}/likes/counts")
    ResponseEntity<LikeCountsResponse> findLikeCounts(@PathVariable long gameId);
}
