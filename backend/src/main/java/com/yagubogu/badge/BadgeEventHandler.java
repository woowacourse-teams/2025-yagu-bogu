package com.yagubogu.badge;

import com.yagubogu.badge.dto.BadgeAwardCandidate;
import com.yagubogu.badge.policy.BadgePolicy;
import com.yagubogu.badge.service.BadgeAwardService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

@RequiredArgsConstructor
@Component
public class BadgeEventHandler {

    private final BadgePolicyRegistry badgePolicyRegistry;
    private final BadgeAwardService badgeAwardService;

    @Async("badgeAsyncExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener
    public void handleBadgeEvent(final BadgeEvent event) {
        BadgePolicy policy = badgePolicyRegistry.getPolicy(event.policy());
        BadgeAwardCandidate candidate = policy.determineAwardCandidate(event);

        if (candidate != null) {
            badgeAwardService.award(candidate);
        }
    }
}
