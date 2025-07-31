package com.yagubogu.checkin.dto;

import java.time.LocalDate;

public record CheckInGameResponse(
        Long checkInId,
        String stadiumFullName,
        CheckInGameTeamResponse homeTeam,
        CheckInGameTeamResponse awayTeam,
        LocalDate attendanceDate
) {
}

