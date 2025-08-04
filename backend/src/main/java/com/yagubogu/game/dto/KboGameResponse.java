package com.yagubogu.game.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.yagubogu.game.domain.GameState;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public record KboGameResponse(
        String gameCode,
        LocalDate gameDate,
        Integer headerNumber,
        LocalTime startAt,
        String stadiumName,
        String awayTeamName,
        String homeTeamName,
        GameState gameState
) {

    @JsonCreator
    public static KboGameResponse create(
            @JsonProperty("G_ID") String gameCode,
            @JsonProperty("G_DT") String gameDate,
            @JsonProperty("HEADER_NO") String headerNumber,
            @JsonProperty("G_TM") String startAt,
            @JsonProperty("S_NM") String stadiumName,
            @JsonProperty("AWAY_NM") String awayTeamName,
            @JsonProperty("HOME_NM") String homeTeamName,
            @JsonProperty("GAME_STATE_SC") String gameState
    ) {
        return new KboGameResponse(
                gameCode,
                LocalDate.parse(gameDate, DateTimeFormatter.BASIC_ISO_DATE),
                Integer.valueOf(headerNumber),
                LocalTime.parse(startAt),
                stadiumName,
                awayTeamName,
                homeTeamName,
                GameState.from(Integer.valueOf(gameState))
        );
    }
}
