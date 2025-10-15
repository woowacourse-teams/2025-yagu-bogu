package com.yagubogu.badge.dto;

import com.yagubogu.badge.domain.Badge;
import com.yagubogu.member.domain.Member;
import java.util.List;

public record BadgeAwardCandidate(
        Member member,
        List<Badge> badges
) {
}
