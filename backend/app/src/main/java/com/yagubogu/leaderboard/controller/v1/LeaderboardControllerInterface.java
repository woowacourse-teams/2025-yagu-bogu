package com.yagubogu.leaderboard.controller.v1;

import com.yagubogu.leaderboard.domain.LeaderboardType;
import com.yagubogu.leaderboard.dto.LeaderboardResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Leaderboard", description = "명예의 전당 API")
@RequestMapping("/leaderboards")
public interface LeaderboardControllerInterface {

    @Operation(summary = "명예 전당 TopN 조회", description = "Type에 해당되는 TopN 랭킹을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "204", description = "데이터 없음")
    })
    @GetMapping
    ResponseEntity<LeaderboardResponse> findTop(
            @RequestParam("type") @NotNull LeaderboardType type,
            @RequestParam(name = "limit", defaultValue = "1") @Positive @Max(10) int limit
    );
}
