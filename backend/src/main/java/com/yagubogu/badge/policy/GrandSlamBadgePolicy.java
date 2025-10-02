package com.yagubogu.badge.policy;

import com.yagubogu.badge.BadgeEvent;
import com.yagubogu.badge.BadgePolicyRegistry;
import com.yagubogu.badge.domain.Badge;
import com.yagubogu.badge.domain.MemberBadge;
import com.yagubogu.badge.domain.Policy;
import com.yagubogu.badge.dto.BadgeAwardCandidate;
import com.yagubogu.badge.repository.BadgeRepository;
import com.yagubogu.badge.repository.MemberBadgeRepository;
import com.yagubogu.checkin.event.StadiumVisitEvent;
import com.yagubogu.checkin.repository.CheckInRepository;
import com.yagubogu.member.domain.Member;
import jakarta.annotation.PostConstruct;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class GrandSlamBadgePolicy implements BadgePolicy {

    private final BadgePolicyRegistry badgePolicyRegistry;
    private final BadgeRepository badgeRepository;
    private final MemberBadgeRepository memberBadgeRepository;
    private final CheckInRepository checkInRepository;

    @PostConstruct
    public void init() {
        badgePolicyRegistry.register(Policy.GRAND_SLAM, this);
    }

    @Override
    public BadgeAwardCandidate determineAwardCandidate(final BadgeEvent event) {
        if (!(event instanceof StadiumVisitEvent visitEvent)) {
            return null;
        }

        Member member = event.member();
        long stadiumId = visitEvent.stadiumId();

        boolean isNewVisit = checkInRepository.isFirstMainStadiumVisit(member, stadiumId);
        if (!isNewVisit) {
            return null;
        }

        List<Badge> notAcquired = badgeRepository.findNotAchievedBadges(member, event.policy());
        if (notAcquired.isEmpty()) {
            return null;
        }

        return new BadgeAwardCandidate(member, notAcquired);
    }

    @Override
    public void award(final BadgeAwardCandidate candidate) {
        Member member = candidate.member();

        for (Badge badge : candidate.badges()) {
            MemberBadge memberBadge = memberBadgeRepository.findByMemberAndBadge(member, badge)
                    .orElseGet(() -> {
                        MemberBadge newBadge = new MemberBadge(badge, member);
                        return memberBadgeRepository.save(newBadge);
                    });
            memberBadge.increaseProgress(badge.getThreshold());
        }
    }
}
