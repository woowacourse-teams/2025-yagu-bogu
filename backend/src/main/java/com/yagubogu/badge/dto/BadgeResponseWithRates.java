package com.yagubogu.badge.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.yagubogu.badge.domain.Policy;
import java.time.LocalDateTime;

public record BadgeResponseWithRates(
        Long id,
        String name,
        String description,
        Policy policy,
        @JsonIgnore int progress,
        boolean acquired,
        LocalDateTime achievedAt,
        double progressRate,
        double achievedRate
) {
    public static BadgeResponseWithRates from(final BadgeRawResponse raw, final long totalMembers) {
        double progressRate = raw.progress() == 0 ? 0.0 : (raw.progress() * 100.0) / raw.threshold();
        double achievedRate = totalMembers == 0 ? 0.0 : (raw.achievedCount() * 100.0) / totalMembers;

        return new BadgeResponseWithRates(
                raw.id(),
                raw.name(),
                raw.description(),
                raw.policy(),
                raw.progress(),
                raw.acquired(),
                raw.createdAt(),
                progressRate,
                achievedRate
        );
    }
}
