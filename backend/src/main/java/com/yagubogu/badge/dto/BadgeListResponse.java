package com.yagubogu.badge.dto;

import com.yagubogu.badge.domain.Badge;
import com.yagubogu.badge.domain.Policy;
import java.util.List;

public record BadgeListResponse(
        RepresentativeBadgeResponse representativeBadge,
        List<BadgeResponseWithRates> badges
) {
    public static BadgeListResponse from(
            final Badge representativeBadge,
            final List<BadgeResponseWithRates> badgeResponses
    ) {
        RepresentativeBadgeResponse representativeBadgeResponse = null;
        if (representativeBadge != null) {
            representativeBadgeResponse = new RepresentativeBadgeResponse(
                    representativeBadge.getId(),
                    representativeBadge.getName(),
                    representativeBadge.getPolicy()
            );
        }

        return new BadgeListResponse(representativeBadgeResponse, badgeResponses);
    }

    public record RepresentativeBadgeResponse(
            Long id,
            String name,
            Policy policy
    ) {
    }
}
