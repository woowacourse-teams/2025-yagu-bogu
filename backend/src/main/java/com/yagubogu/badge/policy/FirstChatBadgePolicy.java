package com.yagubogu.badge.policy;

import com.yagubogu.badge.BadgeEvent;
import com.yagubogu.badge.domain.Badge;
import com.yagubogu.badge.domain.MemberBadge;
import com.yagubogu.badge.domain.Policy;
import com.yagubogu.badge.dto.BadgeAwardCandidate;
import com.yagubogu.badge.repository.BadgeRepository;
import com.yagubogu.badge.repository.MemberBadgeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class FirstChatBadgePolicy implements BadgePolicy {

    private final BadgeRepository badgeRepository;
    private final MemberBadgeRepository memberBadgeRepository;

    @Override
    public BadgeAwardCandidate determineAwardCandidate(final BadgeEvent event) {
        if (event.policy() != Policy.FIRST_CHAT) {
            return null;
        }

        Badge badge = badgeRepository.findByPolicy(Policy.FIRST_CHAT);
        boolean exists = memberBadgeRepository.existsByMemberAndBadge(event.member(), badge);
        if (exists) {
            return null;
        }

        return new BadgeAwardCandidate(event.member(), badge);
    }

    @Override
    public void award(final BadgeAwardCandidate candidate) {
        MemberBadge memberBadge = new MemberBadge(candidate.badge(), candidate.member());
        memberBadge.increaseProgress();
        memberBadgeRepository.save(memberBadge);
    }
}
