package com.yagubogu.member.service;

import com.yagubogu.badge.domain.Badge;
import com.yagubogu.badge.dto.BadgeCountResponse;
import com.yagubogu.badge.dto.BadgeListResponse;
import com.yagubogu.badge.dto.BadgeResponse;
import com.yagubogu.badge.dto.BadgeResponseWithAchievedRate;
import com.yagubogu.badge.repository.BadgeRepository;
import com.yagubogu.badge.repository.MemberBadgeRepository;
import com.yagubogu.global.exception.NotFoundException;
import com.yagubogu.member.domain.Member;
import com.yagubogu.member.dto.MemberFavoriteRequest;
import com.yagubogu.member.dto.MemberFavoriteResponse;
import com.yagubogu.member.dto.MemberInfoResponse;
import com.yagubogu.member.dto.MemberNicknameRequest;
import com.yagubogu.member.dto.MemberNicknameResponse;
import com.yagubogu.member.dto.MemberRepresentativeBadgeResponse;
import com.yagubogu.member.repository.MemberRepository;
import com.yagubogu.team.domain.Team;
import com.yagubogu.team.repository.TeamRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final TeamRepository teamRepository;
    private final BadgeRepository badgeRepository;
    private final MemberBadgeRepository memberBadgeRepository;

    @Transactional
    public MemberNicknameResponse patchNickname(final long memberId, final MemberNicknameRequest request) {
        Member member = getMember(memberId);
        member.updateNickname(request.nickname());

        return new MemberNicknameResponse(member.getNickname());
    }

    @Transactional
    public void removeMember(final Long memberId) {
        Member member = getMember(memberId);

        memberRepository.delete(member);
    }

    public MemberNicknameResponse findNickname(final long memberId) {
        Member member = getMember(memberId);

        return new MemberNicknameResponse(member.getNickname());
    }

    public MemberFavoriteResponse findFavorite(final long memberId) {
        Member member = getMember(memberId);
        Team team = member.getTeam();

        if (team == null) {
            return MemberFavoriteResponse.empty();
        }

        return MemberFavoriteResponse.from(team);
    }

    @Transactional
    public MemberFavoriteResponse updateFavorite(
            final Long memberId,
            final MemberFavoriteRequest memberFavoriteRequest
    ) {
        Member member = getMember(memberId);
        Team team = getTeamByCode(memberFavoriteRequest.teamCode());

        member.updateFavorite(team);

        return MemberFavoriteResponse.from(member.getTeam());
    }

    @Transactional
    public MemberRepresentativeBadgeResponse patchRepresentativeBadge(final Long memberId, final long badgeId) {
        Member member = getMember(memberId);
        Badge badge = getBadge(badgeId);
        validateMemberHasBadge(member, badge);

        member.updateBadge(badge);

        return MemberRepresentativeBadgeResponse.from(badge);
    }

    public BadgeListResponse findBadges(final Long memberId) {
        Member member = getMember(memberId);
        Badge representativeBadge = member.getRepresentativeBadge();
        List<BadgeResponse> badgeResponses = badgeRepository.findAllBadgesWithAcquiredBadgeStatus(memberId);

        long totalMembers = memberRepository.countByDeletedAtIsNull();
        Map<Long, Long> badgeCounts = memberBadgeRepository.countByBadge()
                .stream()
                .collect(Collectors.toMap(BadgeCountResponse::badgeId, BadgeCountResponse::owners));

        List<BadgeResponseWithAchievedRate> badgeResponseWithAchievedRates = badgeResponses.stream()
                .map(badgeResponse -> new BadgeResponseWithAchievedRate(
                        badgeResponse,
                        getNewAchievedRate(badgeResponse, badgeCounts, totalMembers)
                ))
                .toList();

        return BadgeListResponse.from(representativeBadge, badgeResponseWithAchievedRates);
    }

    private double getNewAchievedRate(
            final BadgeResponse badgeResponse,
            final Map<Long, Long> badgeCounts,
            final long totalMembers
    ) {
        if (!badgeCounts.containsKey(badgeResponse.id())) {
            return 0.0;
        }

        return Math.round((100.0 * badgeCounts.get(badgeResponse.id()) / totalMembers) * 10) / 10.0;
    }

    public MemberInfoResponse findMember(final Long memberId) {
        Member member = getMember(memberId);

        return MemberInfoResponse.from(member);
    }

    private void validateMemberHasBadge(Member member, Badge badge) {
        boolean hasBadge = memberBadgeRepository.existsByMemberAndBadge(member, badge);
        if (!hasBadge) {
            throw new NotFoundException("Member does not own this badge");
        }
    }

    private Badge getBadge(final long badgeId) {
        return badgeRepository.findById(badgeId)
                .orElseThrow(() -> new NotFoundException("Badge is not found"));
    }

    private Member getMember(final long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("Member is not found"));
    }

    private Team getTeamByCode(final String teamCode) {
        return teamRepository.findByTeamCode(teamCode)
                .orElseThrow(() -> new NotFoundException("Team is not found"));
    }
}
