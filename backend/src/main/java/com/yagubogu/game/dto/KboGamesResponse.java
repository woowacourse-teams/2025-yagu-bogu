package com.yagubogu.game.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record KboGamesResponse(
        @JsonProperty("game") List<KboGameResponse> games,
        @JsonProperty("code") String statusCode,
        String msg
) {
}
