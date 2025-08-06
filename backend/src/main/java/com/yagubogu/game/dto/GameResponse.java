package com.yagubogu.game.dto;

import java.util.List;

public record GameResponse(
        List<GameWithCheckIn> games
) {
}
