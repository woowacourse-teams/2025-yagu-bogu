package com.yagubogu.sse.dto;

import com.yagubogu.checkin.dto.TeamFanRateParam;
import com.yagubogu.game.domain.Game;

public record GameWithFanRateResponse(
        long gameId,
        TeamFanRateParam homeTeam,
        TeamFanRateParam awayTeam
) {

    public static GameWithFanRateResponse from(final Game game, final double homeTeamRate, final double awayTeamRate) {
        return new GameWithFanRateResponse(
                game.getId(),
                TeamFanRateParam.from(game.getHomeTeam(), homeTeamRate),
                TeamFanRateParam.from(game.getAwayTeam(), awayTeamRate)
        );
    }
}
