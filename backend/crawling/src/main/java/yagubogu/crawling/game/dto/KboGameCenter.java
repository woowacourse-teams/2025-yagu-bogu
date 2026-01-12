package yagubogu.crawling.game.dto;

import java.time.LocalDate;

public record KboGameCenter(
        LocalDate date,
        String status,
        String stadium,
        String startTime
) {
}
