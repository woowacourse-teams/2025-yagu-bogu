package com.yagubogu.stat.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record AverageStatisticResponse(
        double averageRuns,
        double averageAllowedRuns,
        double averageErrors,
        double averageHits,
        double averageAllowedHits
) {

    public static AverageStatisticResponse of(final AverageStatisticResponse response) {
        return new AverageStatisticResponse(
                round(response.averageRuns),
                round(response.averageAllowedRuns),
                round(response.averageErrors),
                round(response.averageHits),
                round(response.averageAllowedHits)
        );
    }

    private static double round(final double value) {
        return BigDecimal.valueOf(value)
                .setScale(1, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
