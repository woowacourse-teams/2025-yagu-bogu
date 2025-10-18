package com.yagubogu.member.dto.v1;

import com.yagubogu.member.domain.Member;
import java.time.LocalDate;

public record MemberProfileResponse(
        String nickname,
        LocalDate enterDate,
        String profileImageUrl,
        String favoriteTeam,
        MemberProfileBadgeResponse representativeBadge,
        VictoryFairyProfileResponse victoryFairy,
        MemberCheckInResponse checkIn
) {
    public static MemberProfileResponse from(
            final Member member,
            final MemberProfileBadgeResponse badge,
            final VictoryFairyProfileResponse victoryFairy,
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
