package com.yagubogu.stadium.dto;

import com.yagubogu.stadium.domain.Stadium;
import java.util.List;

public record StadiumsResponse(
        List<StadiumResponse> stadiums
) {
    public static StadiumsResponse from(final List<Stadium> stadiums) {
        return new StadiumsResponse(
               stadiums.stream()
                       .map(StadiumResponse::from)
                       .toList()
        );
    }
}
