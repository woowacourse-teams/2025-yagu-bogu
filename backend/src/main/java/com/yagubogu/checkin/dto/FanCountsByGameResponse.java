package com.yagubogu.checkin.dto;

public record FanCountsByGameResponse(
        long totalCheckInCounts,
        long homeTeamCheckInCounts,
        long awayTeamCheckInCounts
) {
}
