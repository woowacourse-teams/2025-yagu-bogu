package com.yagubogu.member.dto;

import com.yagubogu.member.domain.Member;
import java.time.LocalDate;

public record MemberProfileResponse(
        String nickname,
        LocalDate enterDate,
        String profileImageUrl,
        String favoriteTeam,
        MemberRepresentativeBadgeResponse representativeBadge,
        MemberVictoryFairyResponse victoryFairy,
        MemberCheckInResponse checkIn
) {
    public static MemberProfileResponse from(
            final Member member,
            final MemberRepresentativeBadgeResponse badge,
            final MemberVictoryFairyResponse victoryFairy,
            final MemberCheckInResponse checkIn) {
        return new MemberProfileResponse(
                member.getNickname().getValue(),
                member.getCreatedAt().toLocalDate(),
                member.getImageUrl(),
                member.getTeam().getShortName(),
                badge,
                victoryFairy,
                checkIn
        );
    }
}
