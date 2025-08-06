package com.yagubogu.checkin.dto;

public record CheckInGameTeamResponse(
        String code,
        String name,
        int score,
        boolean isMyTeam
) {
}
