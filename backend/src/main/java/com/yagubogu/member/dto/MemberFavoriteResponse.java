package com.yagubogu.member.dto;

import com.yagubogu.team.domain.Team;

public record MemberFavoriteResponse(
        String favorite
) {

    public static MemberFavoriteResponse from(final Team team) {
        return new MemberFavoriteResponse(team.getShortName());
    }
}
