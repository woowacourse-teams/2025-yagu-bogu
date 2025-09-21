package com.yagubogu.badge.dto;

import com.yagubogu.badge.domain.Policy;
import java.time.LocalDateTime;

public record BadgeRawResponse(
        Long id,
        String name,
        String description,
        Policy policy,
        int progress,
        boolean acquired,
        LocalDateTime achievedAt,
        long achievedCount,
        int threshold
) {
}