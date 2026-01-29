package com.yagubogu.leaderboard.domain;

public enum LeaderboardType {
    WIN_FAIRY("승리요정"),
    MOST_CHECK_IN("최다직관"),
    CHATTIEST("최다채팅"),
    LOSE_FAIRY("패배요정");

    private final String scoreLabel;

    LeaderboardType(final String scoreLabel) {
        this.scoreLabel = scoreLabel;
    }

    public String getScoreLabel() {
        return scoreLabel;
    }
}
