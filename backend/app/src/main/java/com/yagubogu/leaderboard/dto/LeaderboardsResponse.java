package com.yagubogu.leaderboard.dto;

import java.util.List;

public record LeaderboardsResponse(
        List<LeaderboardResponse> leaderboards
) {
    public static LeaderboardsResponse of(List<LeaderboardResponse> leaderboards) {
        return new LeaderboardsResponse(leaderboards);
    }
}

