package com.yagubogu.game.controller;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;

import com.yagubogu.auth.annotation.RequireRole;
import com.yagubogu.game.dto.ScoreboardResponse;
import com.yagubogu.game.service.crawler.KboScheduleCrawler.GameScheduleSyncService;
import com.yagubogu.game.service.crawler.KboScheduleCrawler.ScheduleType;
import com.yagubogu.game.service.crawler.KboScoardboardCrawler.KboScoreboardService;
import com.yagubogu.member.domain.Role;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private final KboScoreboardService kboScoreboardService;

    @Override
    public ResponseEntity<Void> fetchScheduleRange(
            @RequestParam @DateTimeFormat(iso = DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DATE) LocalDate endDate,
            @RequestParam(defaultValue = "ALL") ScheduleType scheduleType
    ) {
        LocalDateTime now = LocalDateTime.now();
        gameScheduleSyncService.syncByCrawler(now, startDate, endDate, scheduleType);

        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<List<ScoreboardResponse>> fetchScoreboardRange(
            @RequestParam @DateTimeFormat(iso = DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DATE) LocalDate endDate
    ) {
        List<ScoreboardResponse> responses = kboScoreboardService.fetchScoreboardRange(startDate, endDate);
        return ResponseEntity.ok(responses);
    }
}
