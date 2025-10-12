package com.yagubogu.stadium.dto;

import com.yagubogu.game.domain.Game;
import java.util.List;

public record GameResponse(
        long gameId // 필드 추가 예정
) {

    public static List<GameResponse> from(final List<Game> value) {
        return value.stream()
                .map(g -> new GameResponse(g.getId()))
                .toList();
    }
}
