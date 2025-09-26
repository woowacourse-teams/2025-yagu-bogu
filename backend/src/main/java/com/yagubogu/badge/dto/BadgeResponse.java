package com.yagubogu.badge.dto;

import com.yagubogu.badge.domain.Policy;
import java.time.LocalDateTime;

public record BadgeResponse(
        Long id,
        String name,
        String description,
        Policy type,
        int progress,
        boolean owned,
        LocalDateTime achievedAt
) {
}
