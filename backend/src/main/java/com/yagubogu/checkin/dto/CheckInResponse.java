package com.yagubogu.checkin.dto;

import java.time.LocalDate;

public record CheckInResponse(
        Long checkInId,
        String stadiumFullName,
        CheckInGameTeamResponse homeTeam,
        CheckInGameTeamResponse awayTeam,
        LocalDate attendanceDate
) {
}

