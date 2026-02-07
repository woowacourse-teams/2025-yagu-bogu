package com.yagubogu.leaderboard.controller.v1;

import com.yagubogu.auth.annotation.RequireRole;
import com.yagubogu.leaderboard.dto.LeaderboardResponse;
import com.yagubogu.leaderboard.service.LeaderboardService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequireRole
@RestController
public class LeaderboardController implements LeaderboardControllerInterface {

    private final LeaderboardService leaderboardService;

    @Override
    public ResponseEntity<List<LeaderboardResponse>> findAllTop(final int limit) {
        var responses = leaderboardService.findAllTop(limit);
        return ResponseEntity.ok(responses);
    }
}
