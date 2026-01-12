package com.yagubogu.stat.dto;

public record StadiumStatsParam(
        String stadiumName,
        long winCounts,
        long totalCountsWithoutDraw
) {
}
