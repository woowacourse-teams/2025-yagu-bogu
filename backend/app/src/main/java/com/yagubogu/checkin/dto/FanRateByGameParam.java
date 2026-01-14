package com.yagubogu.checkin.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.yagubogu.game.domain.Game;

public record FanRateByGameParam(
        @JsonIgnore long totalCounts,
        long gameId,
        TeamFanRateParam homeTeam,
        TeamFanRateParam awayTeam
) implements Comparable<FanRateByGameParam> {

    public static FanRateByGameParam from(Game game, long totalCounts, double homeTeamRate, double awayTeamRate) {
        return new FanRateByGameParam(
                totalCounts,
                game.getId(),
                TeamFanRateParam.from(game.getHomeTeam(), homeTeamRate),
                TeamFanRateParam.from(game.getAwayTeam(), awayTeamRate)
        );
    }

    @Override
    public int compareTo(FanRateByGameParam other) {
        return Long.compare(other.totalCounts, this.totalCounts);
    }
}
