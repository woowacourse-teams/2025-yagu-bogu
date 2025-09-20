package com.yagubogu.checkin.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.yagubogu.game.domain.Game;

public record FanRateByGameResponse(
        @JsonIgnore long totalCounts,
        @JsonIgnore long gameId,
        TeamFanRateResponse homeTeam,
        TeamFanRateResponse awayTeam
) implements Comparable<FanRateByGameResponse> {

    public static FanRateByGameResponse from(Game game, long totalCounts, double homeTeamRate, double awayTeamRate) {
        return new FanRateByGameResponse(
                totalCounts,
                game.getId(),
                TeamFanRateResponse.from(game.getHomeTeam(), homeTeamRate),
                TeamFanRateResponse.from(game.getAwayTeam(), awayTeamRate)
        );
    }

    @Override
    public int compareTo(FanRateByGameResponse other) {
        return Long.compare(other.totalCounts, this.totalCounts);
    }
}
