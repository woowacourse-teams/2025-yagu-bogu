package com.yagubogu.checkin.dto;

import java.util.List;

public record StadiumCheckInCountsResponse(
        List<StadiumCheckInCountResponse> stadiums
) {
}
