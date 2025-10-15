package com.yagubogu.game.dto;

import java.util.List;

public record KboScoreboardTeam(
        String name,
        Integer runs,
        Integer hits,
        Integer errors,
        Integer basesOnBalls,
        List<String> inningScores
) {
}
