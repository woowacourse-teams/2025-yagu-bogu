package com.yagubogu.badge.service;

import com.yagubogu.badge.dto.BadgeCountResponse;
import com.yagubogu.badge.repository.BadgeRepository;
import com.yagubogu.badge.repository.MemberBadgeRepository;
import com.yagubogu.member.repository.MemberRepository;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class BadgeAchievedRateUpdateScheduler {

    private final BadgeRepository badgeRepository;
    private final MemberRepository memberRepository;
    private final MemberBadgeRepository memberBadgeRepository;

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void updateBadgeRates() {
        long totalMembers = memberRepository.countByDeletedAtIsNull();
        Map<Long, Long> badgeOwnersMap = getBadgeOwnersMap();
        badgeRepository.findAll().forEach(badge -> {
            long currentOwners = badgeOwnersMap.getOrDefault(badge.getId(), 0L);
            if (currentOwners == 0) {
                return;
            }

            double newAchievedRate = getNewAchievedRate(totalMembers, currentOwners);
            badge.updateAchievedRate(newAchievedRate);
        });
    }

    private double getNewAchievedRate(final long totalMembers, final long currentOwners) {
        if (totalMembers == 0) {
            return 0.0;
        }

        return Math.round(100.0 * currentOwners / totalMembers * 10) / 10.0;
    }

    private Map<Long, Long> getBadgeOwnersMap() {
        return memberBadgeRepository.countByBadge()
                .stream()
                .collect(Collectors.toMap(BadgeCountResponse::badgeId, BadgeCountResponse::owners));
    }
}
