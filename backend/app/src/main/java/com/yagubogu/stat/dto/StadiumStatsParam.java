package com.yagubogu.stat.dto;

public record StadiumStatsParam(
        String stadiumName,
        int winCounts,
        int totalCountsWithoutDraw
) {
}
