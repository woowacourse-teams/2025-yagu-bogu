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
public class MemberJoinBadgePolicy implements BadgePolicy {

    private final BadgeRepository badgeRepository;
    private final MemberBadgeRepository memberBadgeRepository;

    @Override
    public BadgeAwardCandidate canAward(final BadgeEvent event) {
        if (event.policy() != Policy.SIGN_UP) {
            return null;
        }

        Badge badge = badgeRepository.findByType(Policy.SIGN_UP);
        boolean exists = memberBadgeRepository.existsByMemberAndBadge(event.member(), badge);
        if (exists) {
            return null;
        }

        return new BadgeAwardCandidate(event.member(), badge);
    }

    @Override
    public void award(final BadgeAwardCandidate candidate) {
        MemberBadge memberBadge = new MemberBadge(candidate.badge(), candidate.member(), 100.0);
        memberBadgeRepository.save(memberBadge);
    }
}
