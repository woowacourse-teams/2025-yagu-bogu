package com.yagubogu.member.service;

import com.yagubogu.global.exception.NotFoundException;
import com.yagubogu.member.domain.Member;
import com.yagubogu.member.dto.MemberFavoriteRequest;
import com.yagubogu.member.dto.MemberFavoriteResponse;
import com.yagubogu.member.repository.MemberRepository;
import com.yagubogu.team.domain.Team;
import com.yagubogu.team.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final TeamRepository teamRepository;

    public void removeMember(final Long memberId) {
        memberRepository.deleteById(memberId);
    }

    public MemberFavoriteResponse findFavorite(final long memberId) {
        Member member = getMember(memberId);
        Team team = member.getTeam();

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

    private Member getMember(final long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("Member is not found"));
    }

    private Team getTeamByCode(final String teamCode) {
        return teamRepository.findByTeamCode(teamCode)
                .orElseThrow(() -> new NotFoundException("Team is not found"));
    }
}
