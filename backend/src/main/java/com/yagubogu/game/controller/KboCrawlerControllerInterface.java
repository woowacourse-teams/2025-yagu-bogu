package com.yagubogu.game.controller;

import com.yagubogu.game.dto.CrawlResponse;
import com.yagubogu.game.service.crawler.KboScheduleCrawler.ScheduleType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "KboCrawler", description = "KBO 크롤링 관련 API")
@RequestMapping("/api/games")
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
}
