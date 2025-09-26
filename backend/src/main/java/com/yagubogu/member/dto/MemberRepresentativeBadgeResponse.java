package com.yagubogu.member.dto;

import com.yagubogu.badge.domain.Badge;

public record MemberRepresentativeBadgeResponse(
        long badgeId
) {

    public static MemberRepresentativeBadgeResponse from(final Badge badge) {
        return new MemberRepresentativeBadgeResponse(badge.getId());
    }
}
