package com.yagubogu.stadium.dto;

import com.yagubogu.team.domain.Team;

public record TeamParam(
        String code,
        String shortName
) {

    public static TeamParam from(final Team team) {
        return new TeamParam(
                team.getTeamCode(),
                team.getShortName()
        );
    }
}
