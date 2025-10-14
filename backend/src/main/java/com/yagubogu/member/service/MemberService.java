package com.yagubogu.member.service;

import com.yagubogu.auth.dto.AuthResponse;
import com.yagubogu.auth.event.SignUpEvent;
import com.yagubogu.badge.domain.Badge;
import com.yagubogu.badge.dto.BadgeListResponse;
import com.yagubogu.badge.dto.BadgeResponseWithRates;
import com.yagubogu.badge.repository.BadgeRepository;
import com.yagubogu.badge.repository.MemberBadgeRepository;
import com.yagubogu.global.exception.ConflictException;
import com.yagubogu.global.exception.NotFoundException;
import com.yagubogu.member.domain.Member;
import com.yagubogu.member.domain.Nickname;
import com.yagubogu.member.dto.MemberFavoriteRequest;
import com.yagubogu.member.dto.MemberFavoriteResponse;
import com.yagubogu.member.dto.MemberFindResult;
import com.yagubogu.member.dto.MemberInfoResponse;
import com.yagubogu.member.dto.MemberNicknameRequest;
import com.yagubogu.member.dto.MemberNicknameResponse;
import com.yagubogu.member.dto.MemberRepresentativeBadgeResponse;
import com.yagubogu.member.repository.MemberRepository;
import com.yagubogu.team.domain.Team;
import com.yagubogu.team.repository.TeamRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
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
    private final ApplicationEventPublisher publisher;

    @Transactional
    public MemberNicknameResponse patchNickname(final long memberId, final MemberNicknameRequest request) {
        Nickname nickname = new Nickname(request.nickname());
        validateDuplicateNickname(nickname);

        Member member = getMember(memberId);
        member.updateNickname(nickname);

        return MemberNicknameResponse.from(member.getNickname());
    }

    @Transactional
    public void removeMember(final Long memberId) {
        Member member = getMember(memberId);

        memberRepository.delete(member);
    }

    public MemberNicknameResponse findNickname(final long memberId) {
        Member member = getMember(memberId);
        Nickname nickname = member.getNickname();

        return MemberNicknameResponse.from(nickname);
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

        long totalMembers = memberRepository.countByDeletedAtIsNull();
        List<BadgeResponseWithRates> badgeResponses = badgeRepository.findAllBadgesWithAchievedCount(memberId)
                .stream()
                .map(raw -> BadgeResponseWithRates.from(raw, totalMembers))
                .toList();

        return BadgeListResponse.from(representativeBadge, badgeResponses);
    }

    public MemberInfoResponse findMember(final Long memberId) {
        Member member = getMember(memberId);

        return MemberInfoResponse.from(member);
    }

    @Transactional
    public MemberFindResult findMember(final AuthResponse response) {
        return memberRepository.findByOauthIdAndDeletedAtIsNull(response.oauthId())
                .map(m -> new MemberFindResult(m, false))
                .orElseGet(() -> {
                    Member savedMember = memberRepository.save(response.toMember());
                    publisher.publishEvent(new SignUpEvent(savedMember));
                    return new MemberFindResult(savedMember, true);
                });
    }

    private void validateMemberHasBadge(final Member member, final Badge badge) {
        boolean hasBadge = memberBadgeRepository.existsByMemberAndBadgeAndIsAchievedTrue(member, badge);
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

    private void validateDuplicateNickname(final Nickname nickname) {
        if (memberRepository.existsByNickname(nickname)) {
            throw new ConflictException("Nickname already exists: " + nickname);
        }
    }
}
