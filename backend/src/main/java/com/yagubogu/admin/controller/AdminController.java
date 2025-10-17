package com.yagubogu.admin.controller;

import com.yagubogu.auth.annotation.RequireRole;
import com.yagubogu.game.service.GameResultSyncService;
import com.yagubogu.game.service.GameScheduleSyncService;
import com.yagubogu.member.domain.Role;
import com.yagubogu.stat.service.StatService;
import com.yagubogu.stat.service.StatSyncService;
import io.swagger.v3.oas.annotations.Hidden;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@Hidden
@RequireRole(Role.ADMIN)
@RequestMapping("/api/admin")
@RestController
public class AdminController {

    private final GameResultSyncService gameResultSyncService;
    private final GameScheduleSyncService gameScheduleSyncService;
    private final StatService statService;
    private final StatSyncService statSyncService;


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

    @PostMapping("/victory-fairy-rankings/sync")
    public ResponseEntity<Void> syncVictoryRankings() {
        int year = LocalDate.now().getYear();
        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);
        for (LocalDate date = startDate; date.isBefore(endDate); date = date.plusDays(1)) {
            statSyncService.updateRankings(date);
        }

        return ResponseEntity.ok().build();
    }
}
