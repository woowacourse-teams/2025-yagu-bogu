package com.yagubogu.game.dto;

import java.time.LocalDate;

public record FailedGameParam(LocalDate date, String gameCode, Long awayTeamId, Long homeTeamId) {
}
