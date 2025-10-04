package com.yagubogu.game.controller;

import com.yagubogu.game.dto.CrawlResponse;
import com.yagubogu.game.dto.ScoreboardResponse;
import com.yagubogu.game.dto.TeamWinRateResponse;
import com.yagubogu.game.service.crawler.KboScheduleCrawler.ScheduleType;
import com.yagubogu.game.service.crawler.KboWinRateCrawler.SeriesType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "KboCrawler", description = "KBO 크롤링 관련 API")
@RequestMapping("/api/kbo")
public interface KboCrawlerControllerInterface {

    @Operation(summary = "특정 날짜의 경기 목록 크롤링", description = "지정한 날짜에 해당하는 경기 정보를 저장합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "경기 조회 성공")
    })
    @PostMapping("/schedule")
    ResponseEntity<CrawlResponse> crawlSchedule(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "ALL") ScheduleType scheduleType
    );

    @Operation(summary = "KBO 팀별 승률 조회", description = "KBO 공식 사이트에서 팀별 승률을 실시간으로 가져옵니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "팀별 승률 조회 성공"),
            @ApiResponse(responseCode = "500", description = "크롤링 중 오류 발생")
    })
    @GetMapping("/teams/win-rate")
    ResponseEntity<TeamWinRateResponse> fetchTeamWinRates(
            @RequestParam(defaultValue = "REGULAR") final SeriesType seriesType
    );

    @Operation(summary = "KBO 스코어보드 조회", description = "지정한 날짜의 스코어보드를 KBO 공식 홈페이지에서 실시간으로 가져옵니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "스코어보드 조회 성공"),
            @ApiResponse(responseCode = "500", description = "크롤링 중 오류 발생")
    })
    @GetMapping("/scoreboard")
    ResponseEntity<ScoreboardResponse> fetchScoreboard(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    );
}
