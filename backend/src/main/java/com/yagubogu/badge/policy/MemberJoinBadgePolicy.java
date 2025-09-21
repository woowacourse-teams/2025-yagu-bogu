package com.yagubogu.badge.policy;

import com.yagubogu.badge.BadgeEvent;
import com.yagubogu.badge.BadgePolicyRegistry;
import com.yagubogu.badge.domain.Badge;
import com.yagubogu.badge.domain.MemberBadge;
import com.yagubogu.badge.domain.Policy;
import com.yagubogu.badge.dto.BadgeAwardCandidate;
import com.yagubogu.badge.repository.BadgeRepository;
import com.yagubogu.badge.repository.MemberBadgeRepository;
import jakarta.annotation.PostConstruct;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class MemberJoinBadgePolicy implements BadgePolicy {

    private final BadgeRepository badgeRepository;
    private final MemberBadgeRepository memberBadgeRepository;
    private final BadgePolicyRegistry badgePolicyRegistry;

    @PostConstruct
    public void init() {
        badgePolicyRegistry.register(Policy.SIGN_UP, this);
    }

    @Override
    public BadgeAwardCandidate determineAwardCandidate(final BadgeEvent event) {
        Badge badge = badgeRepository.findByPolicy(event.policy()).getFirst();
        boolean exists = memberBadgeRepository.existsByMemberAndBadge(event.member(), badge);
        if (exists) {
            return null;
        }

        return new BadgeAwardCandidate(event.member(), List.of(badge));
    }

    @Override
    public void award(final BadgeAwardCandidate candidate) {
        Badge badge = candidate.badges().getFirst();
        MemberBadge memberBadge = new MemberBadge(badge, candidate.member());
        memberBadge.increaseProgress(badge.getThreshold());

        memberBadgeRepository.save(memberBadge);
    }
}
