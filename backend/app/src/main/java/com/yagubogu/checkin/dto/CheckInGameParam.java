package com.yagubogu.checkin.dto;

import com.yagubogu.game.domain.ScoreBoard;
import java.time.LocalDate;

public record CheckInGameParam(
        Long checkInId,
        String stadiumFullName,
        CheckInGameTeamParam homeTeam,
        CheckInGameTeamParam awayTeam,
        LocalDate attendanceDate,
        ScoreBoard homeScoreBoard,
        ScoreBoard awayScoreBoard
) {
}

