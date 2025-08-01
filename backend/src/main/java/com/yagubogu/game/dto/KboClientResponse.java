package com.yagubogu.game.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record KboClientResponse(
        @JsonProperty("game") List<KboGameItemDto> games,
        @JsonProperty("code") String statusCode,
        String msg
) {
}
