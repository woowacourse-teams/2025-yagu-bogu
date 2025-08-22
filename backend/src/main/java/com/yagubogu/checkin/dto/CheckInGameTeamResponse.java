package com.yagubogu.checkin.dto;

public record CheckInGameTeamResponse(
        String code,
        String name,
        Integer score,
        boolean isMyTeam,
        String pitcher
) {
}
