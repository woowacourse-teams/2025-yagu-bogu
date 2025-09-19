package com.yagubogu.sse.dto;

import com.yagubogu.checkin.dto.TeamFanRateResponse;
import com.yagubogu.game.domain.Game;

public record GameWithFanRateResponse(
        long gameId,
        TeamFanRateResponse homeTeam,
        TeamFanRateResponse awayTeam
) {

    public static GameWithFanRateResponse from(final Game game, final double homeTeamRate, final double awayTeamRate) {
        return new GameWithFanRateResponse(
                game.getId(),
                TeamFanRateResponse.from(game.getHomeTeam(), homeTeamRate),
                TeamFanRateResponse.from(game.getAwayTeam(), awayTeamRate)
        );
    }
}
