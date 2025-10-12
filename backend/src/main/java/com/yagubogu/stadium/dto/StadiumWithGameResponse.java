package com.yagubogu.stadium.dto;

import java.util.List;

public record StadiumWithGameResponse(
        Long id,
        String fullName,
        String shortName,
        String location,
        Double latitude,
        Double longitude,
        List<GameResponse> games
) {
}
