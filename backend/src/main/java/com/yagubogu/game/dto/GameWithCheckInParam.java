package com.yagubogu.game.dto;

import java.time.LocalTime;

public record GameWithCheckInParam(
        Long gameId,
        Long totalCheckIns,
        boolean isMyCheckIn,
        StadiumByGameParam stadium,
        TeamByGameParam homeTeam,
        TeamByGameParam awayTeam,
        LocalTime startAt
) {
}

