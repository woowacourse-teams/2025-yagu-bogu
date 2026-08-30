package com.yagubogu.game.controller.v1;

import com.yagubogu.auth.dto.MemberClaims;
import com.yagubogu.game.dto.v1.GameDatesResponse;
import com.yagubogu.game.dto.v1.GameResponse;
import com.yagubogu.game.dto.v1.LiveGamesResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.time.YearMonth;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Game", description = "경기 조회 관련 API")
@RequestMapping("/games")
public interface GameControllerInterface {

    @Operation(summary = "특정 날짜의 경기 목록 조회", description = "지정한 날짜에 해당하는 경기 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "경기 조회 성공"),
            @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음"),
            @ApiResponse(responseCode = "422", description = "미래 날짜는 조회할 수 없음")
    })
    @GetMapping
    ResponseEntity<GameResponse> findGamesByDate(
            @Parameter(hidden = true) MemberClaims memberClaims,
            @RequestParam LocalDate date
    );

    @Operation(summary = "월별 경기 있는 날짜 목록 조회", description = "해당 월에 경기가 있는 날짜만 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "날짜 목록 조회 성공"),
            @ApiResponse(responseCode = "404", description = "회원을 찾을 수 없음")
    })
    @GetMapping("/dates")
    ResponseEntity<GameDatesResponse> findGameDatesByYearMonth(
            @Parameter(hidden = true) MemberClaims memberClaims,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth yearMonth
    );

    @Operation(summary = "현장톡 실시간 경기 목록 조회", description = "당일 전체 경기의 점수와 실시간 경기 상태를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "실시간 경기 목록 조회 성공")
    })
    @GetMapping("/live")
    ResponseEntity<LiveGamesResponse> findLiveGames(
            @Parameter(hidden = true) MemberClaims memberClaims
    );
}
