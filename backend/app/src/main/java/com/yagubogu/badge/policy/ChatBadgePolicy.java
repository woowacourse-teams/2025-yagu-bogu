package com.yagubogu.badge.policy;

import com.yagubogu.badge.BadgePolicyRegistry;
import com.yagubogu.badge.domain.Policy;
import com.yagubogu.badge.repository.BadgeRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class ChatBadgePolicy extends AbstractBadgePolicy {

    private final BadgePolicyRegistry badgePolicyRegistry;

    public ChatBadgePolicy(final BadgeRepository badgeRepository, final BadgePolicyRegistry badgePolicyRegistry) {
        super(badgeRepository);
        this.badgePolicyRegistry = badgePolicyRegistry;
    }

    @PostConstruct
    public void init() {
        badgePolicyRegistry.register(Policy.CHAT, this);
    }
}
