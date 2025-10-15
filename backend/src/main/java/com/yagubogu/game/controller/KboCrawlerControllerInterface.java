package com.yagubogu.game.controller;

import com.yagubogu.game.dto.ScoreboardResponse;
import com.yagubogu.game.service.crawler.KboScheduleCrawler.ScheduleType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "KboCrawler", description = "KBO 크롤링 관련 API")
@RequestMapping("/api/kbo")
public interface KboCrawlerControllerInterface {

    @Operation(summary = "특정 날짜 범위의 경기 목록 크롤링", description = "KBO 공식 사이트에서 지정한 날짜 범위의 경기 정보를 가져옵니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "경기 조회 성공")
    })
    @PostMapping("/schedule")
    ResponseEntity<Void> fetchScheduleRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "ALL") ScheduleType scheduleType
    );

    @Operation(summary = "KBO 스코어보드 조회", description = "KBO 공식 사이트에서 지정한 날짜의 스코어보드를 실시간으로 가져옵니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "스코어보드 조회 성공"),
            @ApiResponse(responseCode = "500", description = "크롤링 중 오류 발생")
    })
    @PostMapping("/scoreboards")
    ResponseEntity<ScoreboardResponse> fetchScoreboard(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    );

    @Operation(summary = "특정 날짜 범위의 경기 목록 크롤링", description = "KBO 공식 사이트에서 지정한 날짜 범위의 경기 정보를 가져옵니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "경기 조회 성공")
    })
    @PostMapping("/scoreboards/range")
    ResponseEntity<List<ScoreboardResponse>> fetchScoreboardRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    );
}
