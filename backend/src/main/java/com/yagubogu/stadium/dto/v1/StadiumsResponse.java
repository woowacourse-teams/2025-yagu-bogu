package com.yagubogu.stadium.dto.v1;

import com.yagubogu.stadium.domain.Stadium;
import com.yagubogu.stadium.dto.StadiumParam;
import java.util.List;

public record StadiumsResponse(
        List<StadiumParam> stadiums
) {
    public static StadiumsResponse from(final List<Stadium> stadiums) {
        return new StadiumsResponse(
                stadiums.stream()
                        .map(StadiumParam::from)
                        .toList()
        );
    }
}
