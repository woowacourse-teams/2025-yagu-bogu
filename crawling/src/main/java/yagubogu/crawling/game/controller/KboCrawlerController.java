package yagubogu.crawling.game.controller;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;

import com.yagubogu.auth.annotation.RequireRole;
import com.yagubogu.game.domain.Game;
import com.yagubogu.member.domain.Role;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import yagubogu.crawling.game.dto.GamesFromGameCenter;
import yagubogu.crawling.game.dto.ScoreboardResponse;
import yagubogu.crawling.game.service.crawler.KboGameCenterCrawler.GameCenterSyncService;
import yagubogu.crawling.game.service.crawler.KboScoardboardCrawler.KboScoreboardService;

@RequiredArgsConstructor
@RequireRole(value = Role.ADMIN)
@RestController
public class KboCrawlerController implements KboCrawlerControllerInterface {

    private final KboScoreboardService kboScoreboardService;
    private final GameCenterSyncService gameCenterSyncService;

    @Override
    public ResponseEntity<List<ScoreboardResponse>> fetchScoreboardRange(
            @RequestParam @DateTimeFormat(iso = DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DATE) LocalDate endDate
    ) {
        List<ScoreboardResponse> responses = kboScoreboardService.fetchScoreboardRange(startDate, endDate);

        return ResponseEntity.ok(responses);
    }

    /**
     * 특정 날짜 경기 상세 정보
     */
    public ResponseEntity<GamesFromGameCenter> fetchGameCenter(
            @RequestParam @DateTimeFormat(iso = DATE) LocalDate date
    ) {
        List<Game> games = gameCenterSyncService.fetchGameCenter(date);

        return ResponseEntity.ok(new GamesFromGameCenter(games));
    }
}
