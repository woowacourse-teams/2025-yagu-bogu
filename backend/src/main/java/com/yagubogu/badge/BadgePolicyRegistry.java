package com.yagubogu.badge;

import com.yagubogu.badge.domain.Policy;
import com.yagubogu.badge.policy.BadgePolicy;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class BadgePolicyRegistry {

    private final Map<Policy, BadgePolicy> policyMap = new HashMap<>();

    public void register(final Policy policy, final BadgePolicy badgePolicy) {
        policyMap.put(policy, badgePolicy);
    }

    public BadgePolicy getPolicy(final Policy policy) {
        return policyMap.get(policy);
    }
}
