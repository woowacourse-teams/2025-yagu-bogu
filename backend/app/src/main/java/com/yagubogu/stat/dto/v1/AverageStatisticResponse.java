package com.yagubogu.stat.dto.v1;

import com.yagubogu.stat.dto.AverageStatisticParam;
import java.math.BigDecimal;
import java.math.RoundingMode;

public record AverageStatisticResponse(
        Double averageRun,
        Double concededRuns,
        Double averageErrors,
        Double averageHits,
        Double concededHits
) {

    public static AverageStatisticResponse from(final AverageStatisticParam averageStatisticParam) {
        return new AverageStatisticResponse(
                round(averageStatisticParam.averageRuns()),
                round(averageStatisticParam.averageConcededRuns()),
                round(averageStatisticParam.averageErrors()),
                round(averageStatisticParam.averageHits()),
                round(averageStatisticParam.averageConcededHits())
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
