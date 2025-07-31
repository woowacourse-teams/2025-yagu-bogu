package com.yagubogu.game.dto;

import java.util.List;

public record KboClientResponse(
        List<KboGameItemDto> game,
        String code,
        String msg
) {
}
