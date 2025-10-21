package com.yagubogu.badge.policy;

import com.yagubogu.badge.BadgeEvent;
import com.yagubogu.badge.BadgePolicyRegistry;
import com.yagubogu.badge.domain.Policy;
import com.yagubogu.badge.repository.BadgeRepository;
import com.yagubogu.checkin.dto.event.StadiumVisitEvent;
import com.yagubogu.checkin.repository.CheckInRepository;
import com.yagubogu.member.domain.Member;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class GrandSlamBadgePolicy extends AbstractBadgePolicy {

    private final BadgePolicyRegistry badgePolicyRegistry;
    private final CheckInRepository checkInRepository;

    public GrandSlamBadgePolicy(
            final BadgePolicyRegistry badgePolicyRegistry,
            final BadgeRepository badgeRepository,
            final CheckInRepository checkInRepository) {
        super(badgeRepository);
        this.badgePolicyRegistry = badgePolicyRegistry;
        this.checkInRepository = checkInRepository;
    }

    @PostConstruct
    public void init() {
        badgePolicyRegistry.register(Policy.GRAND_SLAM, this);
    }

    @Override
    protected boolean isAwardable(final BadgeEvent event) {
        if (!(event instanceof StadiumVisitEvent visitEvent)) {
            return false;
        }

        Member member = event.member();
        long stadiumId = visitEvent.stadiumId();

        return checkInRepository.isFirstMainStadiumVisit(member, stadiumId);
    }
}
