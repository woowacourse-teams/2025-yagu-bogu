package yagubogu.crawling.game.dto;

import static java.time.format.DateTimeFormatter.BASIC_ISO_DATE;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.yagubogu.game.domain.GameState;
import java.time.LocalDate;
import java.time.LocalTime;

public record KboGameParam(
        String gameCode,
        LocalDate gameDate,
        Integer headerNumber,
        LocalTime startAt,
        String stadiumName,
        String awayTeamCode,
        String homeTeamCode,
        GameState gameState
) {

    @JsonCreator
    public static KboGameParam create(
            @JsonProperty("G_ID") String gameCode,
            @JsonProperty("G_DT") String gameDate,
            @JsonProperty("HEADER_NO") String headerNumber,
            @JsonProperty("G_TM") String startAt,
            @JsonProperty("S_NM") String stadiumName,
            @JsonProperty("AWAY_ID") String awayTeamCode,
            @JsonProperty("HOME_ID") String homeTeamCode,
            @JsonProperty("GAME_STATE_SC") String gameState
    ) {
        return new KboGameParam(
                gameCode,
                LocalDate.parse(gameDate, BASIC_ISO_DATE),
                Integer.valueOf(headerNumber),
                LocalTime.parse(startAt),
                stadiumName,
                awayTeamCode,
                homeTeamCode,
                GameState.from(Integer.valueOf(gameState))
        );
    }
}
