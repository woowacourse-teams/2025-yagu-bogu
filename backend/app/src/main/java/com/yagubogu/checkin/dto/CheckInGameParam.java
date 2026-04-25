package com.yagubogu.checkin.dto;

import com.yagubogu.game.domain.GameState;
import com.yagubogu.game.domain.ScoreBoard;
import java.time.LocalDate;
import java.time.LocalTime;

public record CheckInGameParam(
        Long checkInId,
        String stadiumFullName,
        CheckInGameTeamParam homeTeam,
        CheckInGameTeamParam awayTeam,
        LocalDate attendanceDate,
        LocalTime startAt,
        ScoreBoard homeScoreBoard,
        ScoreBoard awayScoreBoard,
        GameState gameState
) {
}
