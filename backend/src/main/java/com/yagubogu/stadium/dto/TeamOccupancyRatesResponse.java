package com.yagubogu.stadium.dto;

import java.util.List;

public record TeamOccupancyRatesResponse(
        List<TeamOccupancyRate> teams
) {
    public record TeamOccupancyRate(
            long id,
            String name,
            double occupancyRate
    ) {
    }
}
