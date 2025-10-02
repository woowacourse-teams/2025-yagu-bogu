package com.yagubogu.badge.policy;

import com.yagubogu.badge.BadgeEvent;
import com.yagubogu.badge.BadgePolicyRegistry;
import com.yagubogu.badge.domain.Badge;
import com.yagubogu.badge.domain.Policy;
import com.yagubogu.badge.dto.BadgeAwardCandidate;
import com.yagubogu.badge.repository.BadgeRepository;
import jakarta.annotation.PostConstruct;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ChatBadgePolicy implements BadgePolicy {

    private final BadgeRepository badgeRepository;
    private final BadgePolicyRegistry badgePolicyRegistry;

    @PostConstruct
    public void init() {
        badgePolicyRegistry.register(Policy.CHAT, this);
    }

    @Override
    public BadgeAwardCandidate determineAwardCandidate(final BadgeEvent event) {
        List<Badge> notAcquired = badgeRepository.findNotAchievedBadges(event.member(), event.policy());
        if (notAcquired.isEmpty()) {
            return null;
        }

        return new BadgeAwardCandidate(event.member(), notAcquired);
    }
}
