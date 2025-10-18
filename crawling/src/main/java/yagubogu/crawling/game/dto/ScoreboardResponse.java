package yagubogu.crawling.game.dto;


import java.time.LocalDate;
import java.util.List;

public record ScoreboardResponse(
        LocalDate date,
        List<KboScoreboardGame> games
) {
}
