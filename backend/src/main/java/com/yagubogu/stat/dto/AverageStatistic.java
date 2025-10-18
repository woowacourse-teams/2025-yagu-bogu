package com.yagubogu.stat.dto;

public record AverageStatistic(
        Double averageRuns,
        Double averageConcededRuns,
        Double averageErrors,
        Double averageHits,
        Double averageConcededHits
) {
}
