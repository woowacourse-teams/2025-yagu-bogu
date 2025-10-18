package com.yagubogu.admin.controller;

import com.yagubogu.auth.annotation.RequireRole;
import com.yagubogu.game.service.GameResultSyncService;
import com.yagubogu.game.service.crawler.KboScheduleCrawler.GameScheduleSyncService;
import com.yagubogu.member.domain.Role;
import io.swagger.v3.oas.annotations.Hidden;
import java.time.LocalDate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Hidden
@RequireRole(Role.ADMIN)
@RequestMapping("/api/admin")
@RestController
public class AdminController {

    private final GameResultSyncService gameResultSyncService;
    private final GameScheduleSyncService gameScheduleSyncService;

    public AdminController(final GameResultSyncService gameResultSyncService,
                           final GameScheduleSyncService gameScheduleSyncService) {
        this.gameResultSyncService = gameResultSyncService;
        this.gameScheduleSyncService = gameScheduleSyncService;
    }

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
