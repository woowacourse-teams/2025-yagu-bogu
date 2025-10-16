package com.yagubogu.game.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record GameUpsertRow(
        String gameCode,
        Long stadiumId,
        Long homeTeamId,
        Long awayTeamId,
        LocalDate date,
        LocalTime startAt,
        Integer homeScore,
        Integer awayScore,
        String homePitcher,
        String awayPitcher,
        String gameState
) {
}
