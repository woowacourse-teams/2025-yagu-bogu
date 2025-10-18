package com.yagubogu.game.dto.v1;

import com.yagubogu.game.dto.GameWithCheckInParam;
import java.util.List;

public record GameResponse(
        List<GameWithCheckInParam> games
) {
}
