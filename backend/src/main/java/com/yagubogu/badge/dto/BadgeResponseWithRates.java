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

    public static BadgeResponseWithRates of(
            final BadgeRawResponse raw,
            final double progressRate,
            final double achievedRate
    ) {
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
