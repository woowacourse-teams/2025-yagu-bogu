package com.yagubogu.badge.dto;

import com.yagubogu.badge.domain.Policy;
import java.time.LocalDateTime;

public record BadgeResponseWithRates(
        Long id,
        String name,
        String description,
        Policy policy,
        boolean acquired,
        LocalDateTime achievedAt,
        double progressRate,
        double achievedRate,
        String badgeImageUrl
) {
    
    public static BadgeResponseWithRates from(final BadgeRawResponse raw, final long totalMembers) {
        double progressRate = raw.progress() == 0 ? 0.0 : (raw.progress() * 100.0) / raw.threshold();
        double achievedRate = totalMembers == 0 ? 0.0 : (raw.achievedCount() * 100.0) / totalMembers;

        return new BadgeResponseWithRates(
                raw.id(),
                raw.name(),
                raw.description(),
                raw.policy(),
                raw.acquired(),
                raw.achievedAt(),
                progressRate,
                achievedRate,
                raw.badgeImageUrl()
        );
    }
}
