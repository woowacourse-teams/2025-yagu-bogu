package com.yagubogu.member.service;

import com.yagubogu.auth.dto.AuthResponse;
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
                    return new MemberFindResult(savedMember, true);
                });
    }

    @Transactional
    public void updateProfileImageUrl(final Long memberId, final String imageUrl) {
        Member member = getMember(memberId);
        member.updateImageUrl(imageUrl);
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
