package com.yagubogu.badge.policy;

import com.yagubogu.badge.BadgeEvent;
import com.yagubogu.badge.domain.Badge;
import com.yagubogu.badge.domain.MemberBadge;
import com.yagubogu.badge.domain.Policy;
import com.yagubogu.badge.repository.BadgeRepository;
import com.yagubogu.badge.repository.MemberBadgeRepository;
import com.yagubogu.member.domain.Member;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class MemberJoinBadgePolicy implements BadgePolicy {

    private final BadgeRepository badgeRepository;
    private final MemberBadgeRepository memberBadgeRepository;

    public MemberJoinBadgePolicy(
            final BadgeRepository badgeRepository,
            final MemberBadgeRepository memberBadgeRepository) {
        this.badgeRepository = badgeRepository;
        this.memberBadgeRepository = memberBadgeRepository;
    }

    @Override
    public boolean canAward(final BadgeEvent event) {
        return event.policy() == Policy.SIGN_UP;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void award(final Member member) {
        Badge badge = badgeRepository.findByType(Policy.SIGN_UP);
        boolean exists = memberBadgeRepository.existsByMemberAndBadge(member, badge);
        if (!exists) {
            MemberBadge memberBadge = new MemberBadge(badge, member, 100.0, null);
            memberBadgeRepository.save(memberBadge);
        }
    }
}
