package com.yagubogu.stat.dto;

import java.util.List;

public record OccupancyRateTotalResponse(
        List<OccupancyRateResponse> teams
) {
    public record OccupancyRateResponse(
            long id,
            String name,
            double occupancyRate
    ) {
    }
}
