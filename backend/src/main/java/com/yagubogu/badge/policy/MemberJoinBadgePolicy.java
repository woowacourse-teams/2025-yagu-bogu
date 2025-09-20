package com.yagubogu.badge.policy;

import com.yagubogu.badge.EventPublished;
import com.yagubogu.badge.domain.Badge;
import com.yagubogu.badge.domain.MemberBadge;
import com.yagubogu.badge.domain.Policy;
import com.yagubogu.badge.dto.BadgeAwardCandidate;
import com.yagubogu.badge.repository.BadgeRepository;
import com.yagubogu.badge.repository.MemberBadgeRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class MemberJoinBadgePolicy implements BadgePolicy {

    private final BadgeRepository badgeRepository;
    private final MemberBadgeRepository memberBadgeRepository;

    @Override
    public BadgeAwardCandidate determineAwardCandidate(final EventPublished event) {
        if (event.policy() != Policy.SIGN_UP) {
            return null;
        }

        Badge badge = badgeRepository.findByPolicy(Policy.SIGN_UP).getFirst();
        boolean exists = memberBadgeRepository.existsByMemberAndBadge(event.member(), badge);
        if (exists) {
            return null;
        }

        return new BadgeAwardCandidate(event.member(), List.of(badge));
    }

    @Override
    public void award(final BadgeAwardCandidate candidate) {
        MemberBadge memberBadge = new MemberBadge(candidate.badges().getFirst(), candidate.member());
        memberBadge.increaseProgress();
        memberBadgeRepository.save(memberBadge);
    }
}
