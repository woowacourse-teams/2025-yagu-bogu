package com.yagubogu.game.controller.v1;

import com.yagubogu.auth.dto.MemberClaims;
import com.yagubogu.game.dto.GameResultParam;
import com.yagubogu.game.dto.v1.GameDatesResponse;
import com.yagubogu.game.dto.v1.GameResponse;
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
import org.springframework.web.bind.annotation.PathVariable;
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

    @Operation(summary = "경기 스코어보드 조회", description = "경기의 스코어보드와 경기중 실시간 상태(현재 타자/투수, 진루정보, 카운트)를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "스코어보드 조회 성공"),
            @ApiResponse(responseCode = "404", description = "경기를 찾을 수 없거나 스코어보드가 아직 없음")
    })
    @GetMapping("/{gameId}/score-board")
    ResponseEntity<GameResultParam> findScoreBoard(
            @Parameter(hidden = true) MemberClaims memberClaims,
            @PathVariable long gameId
    );
}
