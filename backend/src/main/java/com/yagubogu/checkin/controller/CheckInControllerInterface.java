package com.yagubogu.checkin.controller;

import com.yagubogu.auth.dto.MemberClaims;
import com.yagubogu.checkin.domain.CheckInOrderFilter;
import com.yagubogu.checkin.domain.CheckInResultFilter;
import com.yagubogu.checkin.dto.CheckInCountsResponse;
import com.yagubogu.checkin.dto.CheckInHistoryResponse;
import com.yagubogu.checkin.dto.CheckInStatusResponse;
import com.yagubogu.checkin.dto.CreateCheckInRequest;
import com.yagubogu.checkin.dto.FanRateResponse;
import com.yagubogu.checkin.dto.StadiumCheckInCountsResponse;
import com.yagubogu.checkin.dto.VictoryFairyRankingResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "CheckIn", description = "경기 인증 관련 API")
@RequestMapping("/api/check-ins")
public interface CheckInControllerInterface {

    @Operation(summary = "경기 인증 생성", description = "지정한 경기의 인증을 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "인증 생성 성공"),
            @ApiResponse(responseCode = "404", description = "회원, 경기 또는 야구장을 찾을 수 없음")
    })
    @PostMapping
    ResponseEntity<Void> createCheckIn(
            @Parameter(hidden = true) MemberClaims memberClaims,
            @RequestBody CreateCheckInRequest request
    );

    @Operation(summary = "인증 수 조회", description = "연도별로 회원의 총 인증 수를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "인증 수 조회 성공"),
            @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음")
    })
    @GetMapping("/counts")
    ResponseEntity<CheckInCountsResponse> findCheckInCounts(
            @Parameter(hidden = true) MemberClaims memberClaims,
            @RequestParam long year
    );

    @Operation(summary = "인증 내역 조회", description = "연도별 인증 내역을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "인증 내역 조회 성공"),
            @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음")
    })
    @GetMapping("/members")
    ResponseEntity<CheckInHistoryResponse> findCheckInHistory(
            @Parameter(hidden = true) MemberClaims memberClaims,
            @RequestParam int year,
            @RequestParam(name = "result", defaultValue = "ALL") CheckInResultFilter resultFilter,
            @RequestParam(name = "order", defaultValue = "LATEST") CheckInOrderFilter orderFilter
    );

    @Operation(summary = "구장별 팬 점유율 조회", description = "해당 날짜의 구장별 팬 점유율을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "팬 점유율 조회 성공"),
            @ApiResponse(responseCode = "404", description = "회원 또는 경기 정보를 찾을 수 없음")
    })
    @GetMapping("/stadiums/fan-rates")
    ResponseEntity<FanRateResponse> findFanRatesByStadiums(
            @Parameter(hidden = true) MemberClaims memberClaims,
            @RequestParam LocalDate date
    );

    @Operation(summary = "승리 요정 랭킹 조회", description = "전체 유저 중 상위 승리 요정을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "승리 요정 랭킹 조회 성공")
    })
    @GetMapping("/victory-fairy/rankings")
    ResponseEntity<VictoryFairyRankingResponses> findVictoryFairyRankings(
            @Parameter(hidden = true) MemberClaims memberClaims,
            @RequestParam String teamCode
    );

    @Operation(summary = "당일 인증 여부 조회", description = "해당 날짜에 사용자가 인증했는지 여부를 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "인증 여부 조회 성공"),
            @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음")
    })
    @GetMapping("/status")
    ResponseEntity<CheckInStatusResponse> findCheckInStatus(
            @Parameter(hidden = true) MemberClaims memberClaims,
            @RequestParam LocalDate date
    );

    @Operation(summary = "구장별 방문 횟수 조회", description = "사용자의 현재 연도 기준 구장별 체크인 횟수를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "구장별 체크인 횟수 조회 성공"),
            @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음")
    })
    @GetMapping("/stadiums/counts")
    ResponseEntity<StadiumCheckInCountsResponse> findStadiumCheckInCount(
            @Parameter(hidden = true) MemberClaims memberClaims,
            @RequestParam int year
    );
}
