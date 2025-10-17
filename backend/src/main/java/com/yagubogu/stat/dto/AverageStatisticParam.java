package com.yagubogu.stat.dto;

public record AverageStatisticParam(
        Double averageRuns,
        Double averageConcededRuns,
        Double averageErrors,
        Double averageHits,
        Double averageConcededHits
) {
}
