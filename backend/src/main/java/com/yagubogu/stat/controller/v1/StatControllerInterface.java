package com.yagubogu.stat.controller.v1;

import com.yagubogu.auth.dto.MemberClaims;
import com.yagubogu.checkin.dto.v1.TeamFilter;
import com.yagubogu.checkin.dto.v1.VictoryFairyRankingResponse;
import com.yagubogu.stat.dto.v1.AverageStatisticResponse;
import com.yagubogu.stat.dto.v1.LuckyStadiumResponse;
import com.yagubogu.stat.dto.v1.OpponentWinRateResponse;
import com.yagubogu.stat.dto.v1.RecentGamesWinRateResponse;
import com.yagubogu.stat.dto.v1.StatCountsResponse;
import com.yagubogu.stat.dto.v1.WinRateResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Stat", description = "야구 기록 통계 관련 API")
@RequestMapping("/stats")
public interface StatControllerInterface {

    @Operation(summary = "연도별 승패무/인증 횟수 조회", description = "멤버의 해당 연도 승/패/무/인증 횟수를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "승패무 조회 성공"),
            @ApiResponse(responseCode = "403", description = "관리자는 사용할 수 없음"),
            @ApiResponse(responseCode = "404", description = "멤버를 찾을 수 없음")
    })
    @GetMapping("/counts")
    ResponseEntity<StatCountsResponse> findStatCounts(
            @Parameter(hidden = true) MemberClaims memberClaims,
            @RequestParam int year
    );

    @Operation(summary = "내 팀 인증 승률 조회", description = "내 팀의 해당 연도 인증 승률을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "승률 조회 성공"),
            @ApiResponse(responseCode = "403", description = "관리자는 사용할 수 없음"),
            @ApiResponse(responseCode = "404", description = "멤버를 찾을 수 없음")
    })
    @GetMapping("/win-rate")
    ResponseEntity<WinRateResponse> findWinRate(
            @Parameter(hidden = true) MemberClaims memberClaims,
            @RequestParam int year
    );

    @Operation(summary = "내 팀 최근 10경기 인증 승률 조회", description = "내 팀의 최근 10경기 인증 승률을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "승률 조회 성공"),
            @ApiResponse(responseCode = "403", description = "관리자는 사용할 수 없음"),
            @ApiResponse(responseCode = "404", description = "멤버를 찾을 수 없음")
    })
    @GetMapping("/win-rate/recent")
    ResponseEntity<RecentGamesWinRateResponse> findRecentTenGamesWinRate(
            @Parameter(hidden = true) MemberClaims memberClaims,
            @RequestParam int year
    );

    @Operation(summary = "행운의 야구장 조회", description = "해당 연도에서 승률이 가장 높은 야구장을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "행운의 야구장 조회 성공"),
            @ApiResponse(responseCode = "403", description = "관리자는 사용할 수 없음"),
            @ApiResponse(responseCode = "404", description = "멤버를 찾을 수 없음")
    })
    @GetMapping("/lucky-stadiums")
    ResponseEntity<LuckyStadiumResponse> findLuckyStadiums(
            @Parameter(hidden = true) MemberClaims memberClaims,
            @RequestParam int year
    );

    @Operation(summary = "평균 득, 실, 안타, 피안타, 실책 조회", description = "멤버의 전체 경기의 평균 통계 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "평균 경기 통계 조회 성공"),
            @ApiResponse(responseCode = "404", description = "멤버를 찾을 수 없음")
    })
    @GetMapping("/me")
    ResponseEntity<AverageStatisticResponse> findAverageStatistic(
            @Parameter(hidden = true) MemberClaims memberClaims,
            @RequestParam final Integer year
    );

    @Operation(summary = "상대 팀별 승률 조회", description = "상대 팀별 승률 정보 및 전적 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "평균 경기 통계 조회 성공"),
            @ApiResponse(responseCode = "404", description = "멤버를 찾을 수 없음")
    })
    @GetMapping("/win-rate/opponents")
    ResponseEntity<OpponentWinRateResponse> findOpponentWinRate(
            @Parameter(hidden = true) MemberClaims memberClaim,
            @RequestParam final int year
    );

    @Operation(summary = "승리 요정 랭킹 조회", description = "전체 유저 중 상위 승리 요정을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "승리 요정 랭킹 조회 성공")
    })
    @GetMapping("/victory-fairy/rankings")
    ResponseEntity<VictoryFairyRankingResponse> findVictoryFairyRankings(
            @Parameter(hidden = true) MemberClaims memberClaims,
            @RequestParam(name = "team", defaultValue = "ALL") TeamFilter teamFilter,
            @RequestParam(required = false) Integer year
    );
}
