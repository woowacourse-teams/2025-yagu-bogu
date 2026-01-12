package com.yagubogu.internal.service;


import com.yagubogu.auth.dto.MemberClaims;
import com.yagubogu.auth.dto.v1.TokenResponse;
import com.yagubogu.auth.service.RefreshTokenService;
import com.yagubogu.auth.support.AuthTokenProvider;
import com.yagubogu.global.exception.NotFoundException;
import com.yagubogu.member.domain.Member;
import com.yagubogu.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Profile({"local", "dev"})
@RequiredArgsConstructor
@Service
public class InternalAuthService {

    private final MemberRepository memberRepository;
    private final AuthTokenProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;

    public TokenResponse issueAccessToken(final long memberId) {
        Member member = getMember(memberId);

        MemberClaims claims = MemberClaims.from(member);
        String accessToken = jwtProvider.issueAccessToken(claims);
        String refreshToken = refreshTokenService.issue(member);

        return new TokenResponse(accessToken, refreshToken);
    }

    private Member getMember(final long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("Member is not found"));
    }
}
