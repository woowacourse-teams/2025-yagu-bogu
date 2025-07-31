package com.yagubogu.checkin.dto;

public record CheckInGameTeamResponse(
        Long id,
        String name,
        int score,
        boolean isMyTeam
) {
}
