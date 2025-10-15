package com.yagubogu.game.dto;

import java.time.LocalDate;

public record FailedGame(LocalDate date, String gameCode, Long awayTeamId, Long homeTeamId) {
}
