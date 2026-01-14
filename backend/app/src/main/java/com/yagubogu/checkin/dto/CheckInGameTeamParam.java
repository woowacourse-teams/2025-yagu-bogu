package com.yagubogu.checkin.dto;

public record CheckInGameTeamParam(
        String code,
        String name,
        Integer score,
        boolean isMyTeam,
        String pitcher
) {
}
