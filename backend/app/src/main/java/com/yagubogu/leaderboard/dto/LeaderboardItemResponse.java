package com.yagubogu.leaderboard.dto;

public record LeaderboardItemResponse(
        int rank,
        long memberId,
        String nickname,
        String favoriteTeam,
        String profileImageUrl
) {
}
