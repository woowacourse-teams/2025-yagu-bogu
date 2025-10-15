package com.yagubogu.game.dto;


public record ScoreboardGameResponse(
        String gameId,
        String status,
        String stadium,
        String startTime,
        String boxScoreUrl,
        ScoreboardTeamResponse awayTeam,
        ScoreboardTeamResponse homeTeam
) {
}
