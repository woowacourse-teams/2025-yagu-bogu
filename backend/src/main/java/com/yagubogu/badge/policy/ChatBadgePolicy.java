package com.yagubogu.badge.policy;

import com.yagubogu.badge.EventPublished;
import com.yagubogu.badge.domain.Badge;
import com.yagubogu.badge.domain.MemberBadge;
import com.yagubogu.badge.domain.Policy;
import com.yagubogu.badge.dto.BadgeAwardCandidate;
import com.yagubogu.badge.repository.BadgeRepository;
import com.yagubogu.badge.repository.MemberBadgeRepository;
import com.yagubogu.member.domain.Member;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ChatBadgePolicy implements BadgePolicy {

    private final BadgeRepository badgeRepository;
    private final MemberBadgeRepository memberBadgeRepository;

    @Override
    public BadgeAwardCandidate determineAwardCandidate(final EventPublished event) {
        if (event.policy() != Policy.CHAT) {
            return null;
        }

        Member member = event.member();
        List<Badge> badges = badgeRepository.findByPolicy(Policy.CHAT); //해당 뱃지들 찾고
        List<MemberBadge> acquired = memberBadgeRepository.findAcquiredBadges(member, badges); //멤버가 획득한 뱃지
        List<Badge> notAcquired = badges.stream() //여기서의 badge는 chat 정책에 해당하는 뱃지고
                .filter(badge -> acquired.stream().noneMatch(mb -> mb.getBadge().equals(badge)))
                .toList(); //이 뱃지가 아직 멤버에게 획득되지 않았다면 남겨서, 근데 acuiqred는 멤버가 획득한 뱃지니까,
        //notAcquired에는 획득하지 못한 뱃지와 진행중인 뱃지가 있겠네

        if (notAcquired.isEmpty()) { //비어있다는 것은 다 얻었다는 소리
            return null;
        }

        return new BadgeAwardCandidate(event.member(), notAcquired);
    }

    @Override
    public void award(final BadgeAwardCandidate candidate) {
        Member member = candidate.member();

        for (Badge badge : candidate.badges()) {
            memberBadgeRepository.findByMemberAndBadge(member, badge)
                    .ifPresentOrElse(
                            // 이미 존재
                            MemberBadge::increaseProgress,
                            // 없으면 생성 후 저장
                            () -> {
                                MemberBadge newMemberBadge = new MemberBadge(badge, member);
                                newMemberBadge.increaseProgress();
                                memberBadgeRepository.save(newMemberBadge);
                            }
                    );
        }
    }
}
