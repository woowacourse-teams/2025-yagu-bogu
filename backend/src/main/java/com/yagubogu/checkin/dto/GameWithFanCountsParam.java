package com.yagubogu.checkin.dto;

import com.yagubogu.game.domain.Game;

public record GameWithFanCountsParam(
        Game game,
        Long totalCheckInCounts,
        Long homeTeamCheckInCounts,
        Long awayTeamCheckInCounts
) {
}
