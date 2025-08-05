package com.yagubogu.member.service;

import com.yagubogu.global.exception.NotFoundException;
import com.yagubogu.member.domain.Member;
import com.yagubogu.member.dto.MemberFavoriteResponse;
import com.yagubogu.member.repository.MemberRepository;
import com.yagubogu.team.domain.Team;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class MemberService {

    private final MemberRepository memberRepository;

    @Transactional
    public void removeMember(final Long memberId) {
        memberRepository.deleteById(memberId);
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
