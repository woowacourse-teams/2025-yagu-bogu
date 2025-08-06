package com.yagubogu.stat.dto;

public record AverageStatistic(
        Double averageRuns,
        Double averageAllowedRuns,
        Double averageErrors,
        Double averageHits,
        Double averageAllowedHits
) {
}
