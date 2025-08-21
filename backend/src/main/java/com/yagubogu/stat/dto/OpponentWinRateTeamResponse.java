package com.yagubogu.stat.dto;

public record OpponentWinRateTeamResponse(
        Long teamId,
        String name,
        String shortName,
        String teamCode,
        double winRate
) {
}
