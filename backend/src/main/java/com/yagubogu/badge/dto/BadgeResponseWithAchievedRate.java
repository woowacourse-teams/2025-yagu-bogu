package com.yagubogu.badge.dto;

public record BadgeResponseWithAchievedRate(
        BadgeResponse badgeInfo,
        double achievedRate
) {
}
