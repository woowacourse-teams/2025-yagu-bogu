package com.yagubogu.stadium.dto;

import java.util.List;

public record StadiumWithGameParam(
        String shortName,
        String location,
        Double latitude,
        Double longitude,
        TeamParam awayTeam,
        TeamParam homeTeam,
        List<GameParam> games
) {
}
