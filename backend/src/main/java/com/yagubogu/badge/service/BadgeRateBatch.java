package com.yagubogu.badge.service;

import com.yagubogu.badge.domain.BadgeUpdateQueue;
import com.yagubogu.badge.dto.BadgeCountResponse;
import com.yagubogu.badge.repository.BadgeRepository;
import com.yagubogu.badge.repository.BadgeUpdateCounterRepository;
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
public class BadgeRateBatch {

    private final BadgeRepository badgeRepository;
    private final MemberRepository memberRepository;
    private final MemberBadgeRepository memberBadgeRepository;
    private final BadgeUpdateCounterRepository counterRepository;

    @Scheduled(cron = "0 0 5 * * *", zone = "Asia/Seoul")
    @Transactional
    public void updateBadgeRates() {
        BadgeUpdateQueue queue = getBadgeUpdateQueue();
        long pendingCount = queue.getPendingCount();
        if (pendingCount == 0) {
            return;
        }

        long totalMembers = memberRepository.countByDeletedAtIsNull();
        Map<Long, Long> badgeOwnersMap = getBadgeOwnersMap();

        badgeRepository.findAll().forEach(badge -> {
            long ownersBefore = badgeOwnersMap.getOrDefault(badge.getId(), 0L) - pendingCount;
            long ownersAfter = ownersBefore + pendingCount;
            double newAchievedRate = getNewAchievedRate(totalMembers, ownersAfter);

            badge.updateAchievedRate(newAchievedRate);
        });

        queue.reset();
    }

    private double getNewAchievedRate(final long totalMembers, final long ownersAfter) {
        if (totalMembers == 0) {
            return 0.0;
        }

        return Math.round(100.0 * ownersAfter / totalMembers * 10) / 10.0;
    }

    private Map<Long, Long> getBadgeOwnersMap() {
        return memberBadgeRepository.countByBadge()
                .stream()
                .collect(Collectors.toMap(BadgeCountResponse::badgeId, BadgeCountResponse::owners));
    }

    private BadgeUpdateQueue getBadgeUpdateQueue() {
        return counterRepository.findById(1L).orElseThrow();
    }
}
