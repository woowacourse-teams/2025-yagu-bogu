package com.yagubogu.admin.controller;

import com.yagubogu.auth.annotation.RequireRole;
import com.yagubogu.game.service.GameResultSyncService;
import com.yagubogu.member.domain.Role;
import java.time.LocalDate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequireRole(Role.ADMIN)
@RequestMapping("/api/admin")
@RestController
public class AdminController {

    private final GameResultSyncService gameResultSyncService;

    public AdminController(final GameResultSyncService gameResultSyncService) {
        this.gameResultSyncService = gameResultSyncService;
    }

    @PatchMapping("/game-results")
    public ResponseEntity<Void> fetchForceDailyGameSchedule() {
        gameResultSyncService.syncGameResult(LocalDate.now());

        return ResponseEntity.ok().build();
    }
}
