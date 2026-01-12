package com.yagubogu.stat.dto;

public record OpponentWinRateRowParam(
        Long teamId,
        String name,
        String shortName,
        String teamCode,
        int wins,
        int losses,
        int draws
) {
}
