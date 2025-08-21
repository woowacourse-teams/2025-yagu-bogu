package com.yagubogu.checkin.dto;

public record StadiumCheckInCountResponse(
        Long id,
        String location,
        Long checkInCounts
) {
}

