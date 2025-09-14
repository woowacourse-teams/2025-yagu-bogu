package com.yagubogu.badge.policy;

import com.yagubogu.badge.BadgeEvent;
import com.yagubogu.member.domain.Member;

public interface BadgePolicy {
    boolean canAward(BadgeEvent event);

    void award(Member member);
}
