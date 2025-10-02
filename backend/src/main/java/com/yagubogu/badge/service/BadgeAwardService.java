package com.yagubogu.badge.service;

import com.yagubogu.badge.domain.Badge;
import com.yagubogu.badge.domain.MemberBadge;
import com.yagubogu.badge.dto.BadgeAwardCandidate;
import com.yagubogu.badge.repository.MemberBadgeRepository;
import com.yagubogu.member.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class BadgeAwardService {

    private final MemberBadgeRepository memberBadgeRepository;

    @Transactional
    public void award(final BadgeAwardCandidate candidate) {
        Member member = candidate.member();

        for (Badge badge : candidate.badges()) {
            MemberBadge memberBadge = memberBadgeRepository.findByMemberAndBadge(member, badge)
                    .orElseGet(() -> memberBadgeRepository.save(new MemberBadge(badge, member)));
            memberBadge.increaseProgress(badge.getThreshold());
        }
    }
}
