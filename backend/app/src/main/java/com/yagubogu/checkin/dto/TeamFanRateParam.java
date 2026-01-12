package com.yagubogu.checkin.dto;

import com.yagubogu.team.domain.Team;

public record TeamFanRateParam(
        String name,
        String code,
        double fanRate
) {

    public static TeamFanRateParam from(Team team, double rate) {
        return new TeamFanRateParam(
                team.getShortName(),
                team.getTeamCode(),
                rate
        );
    }
}
