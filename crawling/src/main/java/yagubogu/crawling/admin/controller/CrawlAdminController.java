package yagubogu.crawling.admin.controller;

import com.yagubogu.auth.annotation.RequireRole;
import com.yagubogu.member.domain.Role;
import io.swagger.v3.oas.annotations.Hidden;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import yagubogu.crawling.game.service.GameResultSyncService;
import yagubogu.crawling.game.service.crawler.KboScheduleCrawler.GameScheduleSyncService;

@RequiredArgsConstructor
@Hidden
@RequireRole(Role.ADMIN)
@RequestMapping("/admin")
@RestController
public class CrawlAdminController {

    private final GameResultSyncService gameResultSyncService;
    private final GameScheduleSyncService gameScheduleSyncService;


    @PatchMapping("/game-results")
    public ResponseEntity<Void> fetchForceDailyGameResult(
            @RequestParam("date") LocalDate date
    ) {
        gameResultSyncService.syncGameResult(date);

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/game-schedules")
    public ResponseEntity<Void> fetchForceDailyGameSchedule(
            @RequestParam("date") LocalDate date
    ) {
        gameScheduleSyncService.syncGameSchedule(date);

        return ResponseEntity.ok().build();
    }
}
