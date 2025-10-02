package com.yagubogu.badge.policy;

import com.yagubogu.badge.BadgeEvent;
import com.yagubogu.badge.dto.BadgeAwardCandidate;

public interface BadgePolicy {
    BadgeAwardCandidate determineAwardCandidate(BadgeEvent event);
}
