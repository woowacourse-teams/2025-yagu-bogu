package com.yagubogu.badge.dto;

import com.yagubogu.badge.domain.Badge;
import com.yagubogu.member.domain.Member;

public record BadgeAwardCandidate(
        Member member,
        Badge badge
) {
}
