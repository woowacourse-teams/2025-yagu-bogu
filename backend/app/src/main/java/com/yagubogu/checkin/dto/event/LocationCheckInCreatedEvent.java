package com.yagubogu.checkin.dto.event;

public record LocationCheckInCreatedEvent(
        long memberId,
        int gameYear
) {
}
