package com.yagubogu.member.service;

import com.yagubogu.global.exception.NotFoundException;
import com.yagubogu.member.domain.Member;
import com.yagubogu.member.dto.MemberFavoriteResponse;
import com.yagubogu.member.dto.MemberNicknameRequest;
import com.yagubogu.member.dto.MemberNicknameResponse;
import com.yagubogu.member.repository.MemberRepository;
import com.yagubogu.team.domain.Team;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberNicknameResponse patchNickname(final long memberId, final MemberNicknameRequest request) {
        Member member = getMember(memberId);
        member.updateNickname(request.nickname());

        return MemberNicknameResponse.from(member.getNickname());
    }

    public void removeMember(final Long memberId) {
        memberRepository.deleteById(memberId);
    }

    public MemberNicknameResponse findNickname(final long memberId) {
        Member member = getMember(memberId);

        return MemberNicknameResponse.from(member.getNickname());
    }

    public MemberFavoriteResponse findFavorite(final long memberId) {
        Member member = getMember(memberId);
        Team team = member.getTeam();

        return MemberFavoriteResponse.from(team);
    }

    private Member getMember(final long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("Member is not found"));
    }
}
