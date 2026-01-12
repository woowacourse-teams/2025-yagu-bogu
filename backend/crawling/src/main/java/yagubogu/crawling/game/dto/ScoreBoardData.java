package yagubogu.crawling.game.dto;

import java.util.List;

public record ScoreBoardData(
        Integer runs,
        Integer hits,
        Integer errors,
        Integer basesOnBalls,
        String inningScores
) {
    public static ScoreBoardData from(Integer runs, Integer hits, Integer errors,
                                      Integer basesOnBalls, List<String> inningScores) {
        String inningScoresStr = (inningScores == null || inningScores.isEmpty())
                ? ""
                : String.join(",", inningScores);
        return new ScoreBoardData(
                runs != null ? runs : 0,
                hits != null ? hits : 0,
                errors != null ? errors : 0,
                basesOnBalls != null ? basesOnBalls : 0,
                inningScoresStr
        );
    }
}
