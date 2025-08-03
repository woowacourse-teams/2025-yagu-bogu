package com.yagubogu.checkin.dto;

import com.yagubogu.game.domain.Game;

public record FanRateByGameResponse(
        TeamFanRateResponse homeTeam,
        TeamFanRateResponse awayTeam
) {

    public static FanRateByGameResponse from(Game game, double homeTeamRate, double awayTeamRate) {
        return new FanRateByGameResponse(
                TeamFanRateResponse.from(game.getHomeTeam(), homeTeamRate),
                TeamFanRateResponse.from(game.getAwayTeam(), awayTeamRate)
        );
    }
}
