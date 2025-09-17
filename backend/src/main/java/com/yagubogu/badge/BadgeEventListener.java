package com.yagubogu.badge;

import com.yagubogu.badge.dto.BadgeAwardCandidate;
import com.yagubogu.badge.policy.BadgePolicy;
import java.util.List;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class BadgeEventListener {

    private final List<BadgePolicy> badgePolicies;

    public BadgeEventListener(final List<BadgePolicy> badgePolicies) {
        this.badgePolicies = badgePolicies;
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleBadgeEvent(final BadgeEvent event) {
        for (BadgePolicy policy : badgePolicies) {
            BadgeAwardCandidate candidate = policy.canAward(event);
            if (candidate != null) {
                policy.award(candidate);
            }
        }
    }
}
