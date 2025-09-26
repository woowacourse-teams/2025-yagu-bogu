package com.yagubogu.badge.policy;

import com.yagubogu.badge.BadgeEvent;
import com.yagubogu.badge.BadgePolicyRegistry;
import com.yagubogu.badge.domain.Badge;
import com.yagubogu.badge.domain.MemberBadge;
import com.yagubogu.badge.domain.Policy;
import com.yagubogu.badge.dto.BadgeAwardCandidate;
import com.yagubogu.badge.repository.BadgeRepository;
import com.yagubogu.badge.repository.MemberBadgeRepository;
import com.yagubogu.member.domain.Member;
import jakarta.annotation.PostConstruct;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ChatBadgePolicy implements BadgePolicy {

    private final BadgeRepository badgeRepository;
    private final MemberBadgeRepository memberBadgeRepository;
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

    @Override
    public void award(final BadgeAwardCandidate candidate) {
        Member member = candidate.member();

        for (Badge badge : candidate.badges()) {
            MemberBadge memberBadge = memberBadgeRepository.findByMemberAndBadge(member, badge)
                    .orElseGet(() -> {
                        MemberBadge newBadge = new MemberBadge(badge, member);
                        memberBadgeRepository.save(newBadge);
                        return newBadge;
                    });
            memberBadge.increaseProgress(badge.getThreshold());
        }
    }
}
