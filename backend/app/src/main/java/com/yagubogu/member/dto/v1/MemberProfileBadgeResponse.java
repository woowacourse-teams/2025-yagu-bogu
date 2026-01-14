package com.yagubogu.member.dto.v1;

import com.yagubogu.badge.domain.Badge;

public record MemberProfileBadgeResponse(String imageUrl) {

    public static MemberProfileBadgeResponse from(Badge badge) {
        if (badge == null) {
            return null;
        }

        return new MemberProfileBadgeResponse(badge.getBadgeImageUrl());
    }
}
