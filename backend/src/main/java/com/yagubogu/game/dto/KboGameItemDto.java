package com.yagubogu.game.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record KboGameItemDto(
        @JsonProperty("G_ID") String gameCode,
        @JsonProperty("G_DT") String gameDate,
        @JsonProperty("HEADER_NO") Long headerNumber,
        @JsonProperty("G_TM") String startAt,
        @JsonProperty("S_NM") String stadiumName,
        @JsonProperty("AWAY_NM") String awayTeamName,
        @JsonProperty("HOME_NM") String homeTeamName,
        @JsonProperty("GAME_STATE_SC") String gameState,
        @JsonProperty("CANCEL_SC_NM") String cancelState,
        @JsonProperty("GAME_SC_NM") String gameType,
        @JsonProperty("T_SCORE_CN") String homeScore,
        @JsonProperty("B_SCORE_CN") String awayScore
) {

}
