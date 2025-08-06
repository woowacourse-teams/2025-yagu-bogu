package com.yagubogu.game.dto;

public record GameWithCheckIn(
        Long totalCheckIns,
        boolean isMyCheckIn,
        StadiumByGame stadium,
        TeamByGame homeTeam,
        TeamByGame awayTeam
) {
}

