package com.yagubogu.checkin.dto;

import com.yagubogu.team.dto.TeamInfoResponse;
import java.time.LocalDate;

public record CheckInResponse(
        Long checkInId,
        String stadiumFullName,
        TeamInfoResponse homeTeam,
        TeamInfoResponse awayTeam,
        LocalDate attendanceDate
) {
}

