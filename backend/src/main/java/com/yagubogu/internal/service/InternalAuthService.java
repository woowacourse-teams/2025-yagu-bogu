package com.yagubogu.internal.service;


import com.yagubogu.auth.dto.MemberClaims;
import com.yagubogu.auth.dto.TokenResponse;
import com.yagubogu.auth.service.AuthService;
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
    private final AuthService authService;

    public TokenResponse issueAccessToken(long memberId) {
        Member member = getMember(memberId);

        MemberClaims claims = new MemberClaims(memberId, member.getRole());
        String accessToken = jwtProvider.createAccessToken(claims);
        String refreshToken = authService.generateRefreshToken(member);

        return new TokenResponse(accessToken, refreshToken);
    }

    private Member getMember(final long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("member not found: " + memberId));
    }
}
