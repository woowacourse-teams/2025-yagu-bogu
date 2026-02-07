package com.yagubogu.leaderboard.dto;

import com.yagubogu.leaderboard.domain.LeaderboardType;
import java.util.List;

public record LeaderboardResponse(
        LeaderboardType type,
        List<LeaderboardItemResponse> items
) {
    public static LeaderboardResponse of(LeaderboardType type, List<LeaderboardItemResponse> items) {
        return new LeaderboardResponse(type, items);
    }
}
