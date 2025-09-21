package com.yagubogu.badge.policy;

import com.yagubogu.badge.BadgePolicyRegistry;
import com.yagubogu.badge.EventPublished;
import com.yagubogu.badge.domain.Badge;
import com.yagubogu.badge.domain.MemberBadge;
import com.yagubogu.badge.domain.Policy;
import com.yagubogu.badge.dto.BadgeAwardCandidate;
import com.yagubogu.badge.repository.BadgeRepository;
import com.yagubogu.badge.repository.MemberBadgeRepository;
import com.yagubogu.member.domain.Member;
import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
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
    public BadgeAwardCandidate determineAwardCandidate(final EventPublished event) {
        Member member = event.member();
        List<Badge> badges = badgeRepository.findByPolicy(Policy.CHAT); //해당 뱃지들 찾고
        Set<Badge> acquiredSet = memberBadgeRepository.findAcquiredBadges(member, badges)
                .stream()
                .map(MemberBadge::getBadge)
                .collect(Collectors.toSet()); //멤버가 획득한 뱃지들 찾고
        List<Badge> notAcquired = badges.stream()
                .filter(badge -> !acquiredSet.contains(badge))
                .toList(); //획득하지 못한 뱃지들 찾기
        if (notAcquired.isEmpty()) { //획득하지 못한 뱃지가 비어있다는 것은 모든 뱃지를 획득했다는 소리임.
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
            memberBadge.increaseProgress();
        }
    }
}
