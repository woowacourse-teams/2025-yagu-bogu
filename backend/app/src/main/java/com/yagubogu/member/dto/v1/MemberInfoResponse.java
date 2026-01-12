package com.yagubogu.member.dto.v1;

import com.yagubogu.member.domain.Member;
import com.yagubogu.team.domain.Team;
import java.time.LocalDate;

public record MemberInfoResponse(
        String nickname,
        LocalDate createdAt,
        String favoriteTeam,
        String profileImageUrl
) {

    public static MemberInfoResponse from(final Member member) {
        Team team = member.getTeam();
        if (team == null) {
            return new MemberInfoResponse(
                    member.getNickname().getValue(),
                    member.getCreatedAt().toLocalDate(),
                    null,
                    member.getImageUrl()
            );
        }

        return new MemberInfoResponse(
                member.getNickname().getValue(),
                member.getCreatedAt().toLocalDate(),
                member.getTeam().getShortName(),
                member.getImageUrl()
        );
    }
}
