package com.yagubogu.game.controller;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;

import com.yagubogu.game.dto.CrawlResponse;
import com.yagubogu.game.dto.TeamWinRateResponse;
import com.yagubogu.game.service.GameScheduleSyncService;
import com.yagubogu.game.service.TeamWinRateService;
import com.yagubogu.game.service.crawler.KboScheduleCrawler.ScheduleType;
import com.yagubogu.game.service.crawler.KboWinRateCrawler.SeriesType;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class KboCrawlerController implements KboCrawlerControllerInterface {

    private final GameScheduleSyncService gameScheduleSyncService;
    private final TeamWinRateService teamWinRateService;

    public ResponseEntity<CrawlResponse> crawlSchedule(
            @RequestParam @DateTimeFormat(iso = DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DATE) LocalDate endDate,
            @RequestParam(defaultValue = "ALL") ScheduleType scheduleType
    ) {
        LocalDate now = LocalDate.now();
        int crawled = gameScheduleSyncService.syncByCrawler(now, startDate, endDate, scheduleType);

        return ResponseEntity.ok(new CrawlResponse(crawled));
    }

    @Override
    public ResponseEntity<TeamWinRateResponse> fetchTeamWinRates(
            @RequestParam(defaultValue = "REGULAR") final SeriesType seriesType
    ) {
        TeamWinRateResponse response = teamWinRateService.fetchTeamWinRates(seriesType);
        return ResponseEntity.ok(response);
    }
}

