package com.yagubogu.game.event;

import java.time.LocalDate;

public record ETLCompletedEvent(
        LocalDate date,
        String stadium,
        String homeTeam,
        String awayTeam
) {
}
