package com.yagubogu.badge.policy;

import com.yagubogu.badge.BadgeEvent;
import com.yagubogu.badge.dto.BadgeAwardCandidate;

public interface BadgePolicy {
    BadgeAwardCandidate canAward(BadgeEvent event);

    void award(BadgeAwardCandidate member);
}
