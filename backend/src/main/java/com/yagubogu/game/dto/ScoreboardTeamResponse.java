package com.yagubogu.game.dto;

import java.util.List;

public record ScoreboardTeamResponse(
        String name,
        Integer runs,
        Integer hits,
        Integer errors,
        Integer basesOnBalls,
        List<String> inningScores
) {
}
