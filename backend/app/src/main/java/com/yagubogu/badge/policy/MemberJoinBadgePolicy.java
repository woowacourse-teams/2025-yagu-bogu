package com.yagubogu.badge.policy;

import com.yagubogu.badge.BadgePolicyRegistry;
import com.yagubogu.badge.domain.Policy;
import com.yagubogu.badge.repository.BadgeRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class MemberJoinBadgePolicy extends AbstractBadgePolicy {

    private final BadgePolicyRegistry badgePolicyRegistry;

    public MemberJoinBadgePolicy(final BadgeRepository badgeRepository, final BadgePolicyRegistry badgePolicyRegistry) {
        super(badgeRepository);
        this.badgePolicyRegistry = badgePolicyRegistry;
    }

    @PostConstruct
    public void init() {
        badgePolicyRegistry.register(Policy.SIGN_UP, this);
    }
}
