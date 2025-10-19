package yagubogu.crawling.game.controller;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;

import com.yagubogu.auth.annotation.RequireRole;
import com.yagubogu.member.domain.Role;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import yagubogu.crawling.game.dto.ScoreboardResponse;
import yagubogu.crawling.game.service.crawler.KboGameCenterCrawler.GameCenter;
import yagubogu.crawling.game.service.crawler.KboGameCenterCrawler.GameCenterSyncService;
import yagubogu.crawling.game.service.crawler.KboScheduleCrawler.GameScheduleSyncService;
import yagubogu.crawling.game.service.crawler.KboScheduleCrawler.ScheduleType;
import yagubogu.crawling.game.service.crawler.KboScoardboardCrawler.KboScoreboardService;

@RequiredArgsConstructor
@RequireRole(value = Role.ADMIN)
@RestController
public class KboCrawlerController implements KboCrawlerControllerInterface {

    private final GameScheduleSyncService gameScheduleSyncService;
    private final KboScoreboardService kboScoreboardService;
    private final GameCenterSyncService gameCenterSyncService;

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

    /**
     * 오늘 경기 상세 정보
     */
    @GetMapping("/today")
    public ResponseEntity<GameCenter> getTodayData() {
        GameCenter data = gameCenterSyncService.getTodayGameDetails();

        return ResponseEntity.ok(data);
    }

    /**
     * 특정 날짜 경기 상세 정보
     */
    @GetMapping("/{date}")
    public ResponseEntity<GameCenter> getDateData(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        GameCenter data = gameCenterSyncService.getGameDetails(date);

        return ResponseEntity.ok(data);
    }
}
