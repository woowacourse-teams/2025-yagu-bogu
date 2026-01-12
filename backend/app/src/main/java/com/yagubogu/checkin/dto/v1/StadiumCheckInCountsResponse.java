package com.yagubogu.checkin.dto.v1;

import com.yagubogu.checkin.dto.StadiumCheckInCountParam;
import java.util.List;

public record StadiumCheckInCountsResponse(
        List<StadiumCheckInCountParam> stadiums
) {
}
