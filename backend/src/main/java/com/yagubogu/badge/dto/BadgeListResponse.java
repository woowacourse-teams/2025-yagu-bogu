package com.yagubogu.badge.dto;

import com.yagubogu.badge.domain.Badge;
import com.yagubogu.badge.domain.Policy;
import java.util.List;

public record BadgeListResponse(
        RepresentativeBadgeResponse representativeBadge,
        List<BadgeResponse> badges
) {
    public static BadgeListResponse from(final Badge representativeBadge, final List<BadgeResponse> badges) {
        RepresentativeBadgeResponse representativeBadgeResponse = null;
        if (representativeBadge == null) {
            return new BadgeListResponse(representativeBadgeResponse, badges);
        }
        representativeBadgeResponse = new RepresentativeBadgeResponse(
                representativeBadge.getId(), representativeBadge.getName(), representativeBadge.getType());

        return new BadgeListResponse(representativeBadgeResponse, badges);
    }

    private record RepresentativeBadgeResponse(
            Long id,
            String name,
            Policy type
    ) {
    }
}
