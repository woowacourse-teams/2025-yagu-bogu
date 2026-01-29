package com.yagubogu.leaderboard.dto;

public record LeaderboardRow(
        long rank,
        long memberId,
        String nickname,
        String favoriteTeam,
        String profileImageUrl,
        double score
) {
}
