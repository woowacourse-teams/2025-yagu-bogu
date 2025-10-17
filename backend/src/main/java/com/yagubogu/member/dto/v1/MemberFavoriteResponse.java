package com.yagubogu.member.dto.v1;

import com.yagubogu.team.domain.Team;

public record MemberFavoriteResponse(
        String favorite
) {

    public static MemberFavoriteResponse from(final Team team) {
        return new MemberFavoriteResponse(team.getShortName());
    }

    public static MemberFavoriteResponse empty() {
        return new MemberFavoriteResponse(null);
    }
}
