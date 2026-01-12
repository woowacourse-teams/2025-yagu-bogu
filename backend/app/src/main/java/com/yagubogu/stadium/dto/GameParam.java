package com.yagubogu.stadium.dto;

import com.yagubogu.game.domain.Game;
import java.util.List;

public record GameParam(
        long gameId
) {

    public static List<GameParam> from(final List<Game> value) {
        return value.stream()
                .map(g -> new GameParam(g.getId()))
                .toList();
    }
}
