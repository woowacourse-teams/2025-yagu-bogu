package com.yagubogu.checkin.dto;

import com.yagubogu.team.domain.Team;

public record TeamFanRateResponse(
        String name,
        String code,
        double fanRate
) {

    public static TeamFanRateResponse from(Team team, double rate) {
        return new TeamFanRateResponse(
                team.getShortName(),
                team.getTeamCode(),
                rate
        );
    }
}
