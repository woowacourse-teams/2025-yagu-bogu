package com.yagubogu.stat.dto;

public record StadiumStatsDto(
        String stadiumName,
        long winCounts,
        long totalCountsWithoutDraw
) {
}
