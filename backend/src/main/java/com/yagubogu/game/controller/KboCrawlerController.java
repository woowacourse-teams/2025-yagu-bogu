package com.yagubogu.game.controller;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;

import com.yagubogu.auth.annotation.RequireRole;
import com.yagubogu.game.dto.ScheduleResponse;
import com.yagubogu.game.dto.ScoreboardResponse;
import com.yagubogu.game.dto.TeamWinRateResponse;
import com.yagubogu.game.service.crawler.KboScheduleCrawler.GameScheduleSyncService;
import com.yagubogu.game.service.crawler.KboScheduleCrawler.ScheduleType;
import com.yagubogu.game.service.crawler.KboScoardboardCrawler.KboScoreboardService;
import com.yagubogu.game.service.crawler.KboWinRateCrawler.SeriesType;
import com.yagubogu.game.service.crawler.KboWinRateCrawler.TeamWinRateService;
import com.yagubogu.member.domain.Role;
import io.micrometer.core.annotation.Timed;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequireRole(value = Role.ADMIN)
@RestController
public class KboCrawlerController implements KboCrawlerControllerInterface {

    private final GameScheduleSyncService gameScheduleSyncService;
    private final TeamWinRateService teamWinRateService;
    private final KboScoreboardService kboScoreboardService;

    @Override
    @Timed(value = "api.schedule.range", description = "스케줄 범위 호출 지연")
    public ResponseEntity<ScheduleResponse> fetchScheduleRange(
            @RequestParam @DateTimeFormat(iso = DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DATE) LocalDate endDate,
            @RequestParam(defaultValue = "ALL") ScheduleType scheduleType
    ) {
        LocalDate now = LocalDate.now();
        int crawled = gameScheduleSyncService.syncByCrawler(now, startDate, endDate, scheduleType);

        return ResponseEntity.ok(new ScheduleResponse(crawled));
    }

    @Override
    @Timed(value = "api.team.winrates", description = "팀 승률 호출 지연")
    public ResponseEntity<TeamWinRateResponse> fetchTeamWinRates(
            @RequestParam(defaultValue = "REGULAR") final SeriesType seriesType
    ) {
        TeamWinRateResponse response = teamWinRateService.fetchTeamWinRates(seriesType);
        return ResponseEntity.ok(response);
    }

    @Override
    @Timed(value = "api.scoreboard.one", description = "단일 날짜 스코어보드 호출 지연")
    public ResponseEntity<ScoreboardResponse> fetchScoreboard(
            @RequestParam @DateTimeFormat(iso = DATE) final LocalDate date
    ) {
        ScoreboardResponse response = kboScoreboardService.fetchScoreboard(date);
        return ResponseEntity.ok(response);
    }

    @Override
    @Timed(value = "api.scoreboard.range", description = "범위 스코어보드 호출 지연")
    public ResponseEntity<List<ScoreboardResponse>> fetchScoreboardRange(
            @RequestParam @DateTimeFormat(iso = DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DATE) LocalDate endDate
    ) {
        List<ScoreboardResponse> responses = kboScoreboardService.fetchScoreboardRange(startDate, endDate);
        return ResponseEntity.ok(responses);
    }
}
