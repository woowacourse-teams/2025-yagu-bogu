package com.yagubogu.stadium.dto;

import java.util.List;

public record StadiumWithGameParam(
        String name,
        Double latitude,
        Double longitude,
        List<GameParam> games
) {
}
