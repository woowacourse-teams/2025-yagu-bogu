package com.yagubogu.badge.dto;

import com.yagubogu.badge.domain.Badge;
import com.yagubogu.badge.domain.Policy;
import java.util.List;

public record BadgeListResponse(
        RepresentativeBadgeResponse representativeBadge,
        List<BadgeResponseWithAchievedRate> badges
) {
    public static BadgeListResponse from(
            final Badge representativeBadge,
            final List<BadgeResponseWithAchievedRate> badgeResponseWithAchievedRates
    ) {
        RepresentativeBadgeResponse representativeBadgeResponse = null;
        if (representativeBadge == null) {
            return new BadgeListResponse(representativeBadgeResponse, badgeResponseWithAchievedRates);
        }
        representativeBadgeResponse = new RepresentativeBadgeResponse(
                representativeBadge.getId(), representativeBadge.getName(), representativeBadge.getType());

        return new BadgeListResponse(representativeBadgeResponse, badgeResponseWithAchievedRates);
    }

    private record RepresentativeBadgeResponse(
            Long id,
            String name,
            Policy type
    ) {
    }
}
