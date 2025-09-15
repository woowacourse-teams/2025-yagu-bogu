package com.yagubogu.member.service;

import com.yagubogu.auth.dto.AuthResponse;
import com.yagubogu.global.exception.NotFoundException;
import com.yagubogu.member.domain.Member;
import com.yagubogu.member.dto.MemberFavoriteRequest;
import com.yagubogu.member.dto.MemberFavoriteResponse;
import com.yagubogu.member.dto.MemberInfoResponse;
import com.yagubogu.member.dto.MemberNicknameRequest;
import com.yagubogu.member.dto.MemberNicknameResponse;
import com.yagubogu.member.dto.UpsertResult;
import com.yagubogu.member.repository.MemberRepository;
import com.yagubogu.team.domain.Team;
import com.yagubogu.team.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final TeamRepository teamRepository;

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

    public MemberInfoResponse findMember(final Long memberId) {
        Member member = getMember(memberId);

        return MemberInfoResponse.from(member);
    }

    @Transactional
    public UpsertResult upsertMember(final AuthResponse response) {
        return memberRepository.findByOauthIdAndDeletedAtIsNull(response.oauthId())
                .map(m -> new UpsertResult(m, false))
                .orElseGet(() -> new UpsertResult(memberRepository.save(response.toMember()), true));
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
