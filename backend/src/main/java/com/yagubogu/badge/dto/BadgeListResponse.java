package com.yagubogu.badge.dto;

import java.util.List;

public record BadgeListResponse(
        List<BadgeResponse> badges
) {
    public static BadgeListResponse from(final List<BadgeResponse> badges) {
        return new BadgeListResponse(badges);
    }
}
