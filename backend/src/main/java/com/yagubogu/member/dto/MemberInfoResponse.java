package com.yagubogu.member.dto;

import com.yagubogu.member.domain.Member;
import java.time.LocalDate;

public record MemberInfoResponse(
        String nickname,
        LocalDate createdAt,
        String favoriteTeam,
        String profileImageUrl
) {

    public static MemberInfoResponse from(final Member member) {
        return new MemberInfoResponse(
                member.getNickname(),
                member.getCreatedAt().toLocalDate(),
                member.getTeam().getShortName(),
                member.getImageUrl()
        );
    }

    public static MemberInfoResponse fromNullableTeam(final Member member) {
        return new MemberInfoResponse(
                member.getNickname(),
                member.getCreatedAt().toLocalDate(),
                null,
                member.getImageUrl()
        );
    }
}
