package com.yagubogu.checkin.dto;

import com.yagubogu.game.domain.ScoreBoard;
import java.time.LocalDate;

public record CheckInGameResponse(
        Long checkInId,
        String stadiumFullName,
        CheckInGameTeamResponse homeTeam,
        CheckInGameTeamResponse awayTeam,
        LocalDate attendanceDate,
        ScoreBoard homeScoreBoard,
        ScoreBoard awayScoreBoard
) {
}

