package com.yagubogu.stat.dto;

public record OpponentWinRateRow(
        Long teamId,
        String name,
        String shortName,
        String teamCode,
        long wins,
        long games
) {
}
