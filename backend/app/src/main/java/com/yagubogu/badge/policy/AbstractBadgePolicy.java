package com.yagubogu.badge.policy;

import com.yagubogu.badge.BadgeEvent;
import com.yagubogu.badge.domain.Badge;
import com.yagubogu.badge.dto.BadgeAwardCandidate;
import com.yagubogu.badge.repository.BadgeRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class AbstractBadgePolicy implements BadgePolicy {

    private final BadgeRepository badgeRepository;

    @Override
    public BadgeAwardCandidate determineAwardCandidate(final BadgeEvent event) {
        if (!isAwardable(event)) {
            return null;
        }

        List<Badge> notAcquired = badgeRepository.findNotAchievedBadges(event.member(), event.policy());
        if (notAcquired.isEmpty()) {
            return null;
        }

        return new BadgeAwardCandidate(event.member(), notAcquired);
    }

    protected boolean isAwardable(final BadgeEvent event) {
        return true;
    }
}
