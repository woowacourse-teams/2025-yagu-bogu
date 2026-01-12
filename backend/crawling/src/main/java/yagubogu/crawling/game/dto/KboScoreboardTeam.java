package yagubogu.crawling.game.dto;

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
