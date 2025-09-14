package com.yagubogu.stat.dto;

public record OpponentWinRateRow(
        Long teamId,
        String name,
        String shortName,
        String teamCode,
        Integer wins,
        Integer losses,
        Integer draws
) {
}
