package com.yagubogu.badge;

import com.yagubogu.badge.policy.BadgePolicy;
import com.yagubogu.member.domain.Member;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class BadgeEventListener {

    private final List<BadgePolicy> badgePolicies;

    public BadgeEventListener(final List<BadgePolicy> badgePolicies) {
        this.badgePolicies = badgePolicies;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleBadgeEvent(final BadgeEvent event) {
        Member member = event.memberId();
        for (BadgePolicy policy : badgePolicies) {
            if (policy.canAward(event)) {
                policy.award(member);
            }
        }
    }
}
