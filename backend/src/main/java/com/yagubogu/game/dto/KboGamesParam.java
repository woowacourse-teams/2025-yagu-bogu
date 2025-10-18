package com.yagubogu.game.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record KboGamesParam(
        @JsonProperty("game") List<KboGameParam> games,
        @JsonProperty("code") String statusCode,
        String msg
) {
}
