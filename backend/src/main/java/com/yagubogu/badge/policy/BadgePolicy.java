package com.yagubogu.badge.policy;

import com.yagubogu.badge.EventPublished;
import com.yagubogu.badge.dto.BadgeAwardCandidate;

public interface BadgePolicy {
    BadgeAwardCandidate determineAwardCandidate(EventPublished event);

    void award(BadgeAwardCandidate member);
}
