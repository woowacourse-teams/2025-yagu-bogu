package com.yagubogu.leaderboard.controller.v1;

import com.yagubogu.auth.annotation.RequireRole;
import com.yagubogu.leaderboard.domain.LeaderboardType;
import com.yagubogu.leaderboard.dto.LeaderboardResponse;
import com.yagubogu.leaderboard.service.LeaderboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequireRole
@RestController
public class LeaderboardController implements LeaderboardControllerInterface {

    private final LeaderboardService leaderboardService;

    @Override
    public ResponseEntity<LeaderboardResponse> findTop(final LeaderboardType type, final int limit) {

        LeaderboardResponse response = leaderboardService.findTop(type, limit);
        return ResponseEntity.ok(response);
    }
}
