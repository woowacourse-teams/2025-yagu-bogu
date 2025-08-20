package com.yagubogu.stat.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record AverageStatisticResponse(
        Double averageRun,
        Double concededRuns,
        Double averageErrors,
        Double averageHits,
        Double concededHits
) {

    public static AverageStatisticResponse from(final AverageStatistic averageStatistic) {
        return new AverageStatisticResponse(
                round(averageStatistic.averageRuns()),
                round(averageStatistic.averageConcededRuns()),
                round(averageStatistic.averageErrors()),
                round(averageStatistic.averageHits()),
                round(averageStatistic.averageConcededHits())
        );
    }

    private static Double round(final Double value) {
        if (value == null) {
            return null;
        }

        return BigDecimal.valueOf(value)
                .setScale(1, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
