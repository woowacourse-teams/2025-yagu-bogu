package com.yagubogu.badge;

import com.yagubogu.badge.domain.Policy;
import com.yagubogu.member.domain.Member;

public record EventPublished(
        Member member,
        Policy policy
) {
}
