package com.yagubogu.badge;

import com.yagubogu.badge.dto.BadgeAwardCandidate;
import com.yagubogu.badge.policy.BadgePolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@RequiredArgsConstructor
@Component
public class BadgeEventListener {

    private final BadgePolicyRegistry badgePolicyRegistry;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleBadgeEvent(final EventPublished event) {
        BadgePolicy policy = badgePolicyRegistry.getPolicy(event.policy());
        executePolicy(policy, event);
    }

    private void executePolicy(final BadgePolicy policy, final EventPublished event) {
        BadgeAwardCandidate candidate = policy.determineAwardCandidate(event);
        if (candidate != null) {
            policy.award(candidate);
        }
    }
}
