package com.yagubogu.leaderboard.controller.v1;

import com.yagubogu.auth.annotation.RequireRole;
import com.yagubogu.leaderboard.dto.LeaderboardsResponse;
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
    public ResponseEntity<LeaderboardsResponse> findAllTop(final int limit) {
        var responses = leaderboardService.findAllTop(limit);
        return ResponseEntity.ok(LeaderboardsResponse.of(responses));
    }
}
