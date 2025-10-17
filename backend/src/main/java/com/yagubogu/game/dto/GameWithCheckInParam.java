package com.yagubogu.game.dto;

public record GameWithCheckInParam(
        Long gameId,
        Long totalCheckIns,
        boolean isMyCheckIn,
        StadiumByGameParam stadium,
        TeamByGameParam homeTeam,
        TeamByGameParam awayTeam
) {
}

