package com.yagubogu.stadium.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.List;

public record TeamOccupancyRatesResponse(
        List<TeamOccupancyRate> teams
) {
    public record TeamOccupancyRate(
            long id,
            String name,
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "0.0") double occupancyRate
    ) {
    }
}
