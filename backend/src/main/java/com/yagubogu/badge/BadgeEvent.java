package com.yagubogu.badge;

import com.yagubogu.badge.domain.Policy;
import com.yagubogu.member.domain.Member;

public record BadgeEvent(
        Member member,
        Policy policy
) {
}
