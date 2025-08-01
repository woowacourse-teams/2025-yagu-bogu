package com.yagubogu.auth.service;

import com.yagubogu.auth.client.AuthGateway;
import com.yagubogu.auth.dto.AuthResponse;
import com.yagubogu.auth.dto.LoginRequest;
import com.yagubogu.auth.dto.LoginResponse;
import com.yagubogu.auth.dto.LoginResponse.MemberResponse;
import com.yagubogu.auth.dto.MemberInfo;
import com.yagubogu.auth.exception.InvalidTokenException;
import com.yagubogu.auth.token.JwtProvider;
import com.yagubogu.global.config.GoogleAuthProperties;
import com.yagubogu.member.domain.Member;
import com.yagubogu.member.repository.MemberRepository;
import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AuthService {

    private static final String ISSUER_GOOGLE = "https://accounts.google.com";
    private static final String ISSUER_GOOGLE_NO_SCHEME = "accounts.google.com";

    private final MemberRepository memberRepository;
    private final AuthGateway authGateway;
    private final JwtProvider jwtProvider;
    private final GoogleAuthProperties googleAuthProperties;

    public LoginResponse login(final LoginRequest request) {
        AuthResponse response = authGateway.validateToken(request);
        validateTokenClaim(response);

        Optional<Member> memberOptional = memberRepository.findBySub(response.sub());
        boolean isExisting = memberOptional.isPresent();
        Member member = memberOptional.orElseGet(() -> memberRepository.save(response.toMember()));
        MemberInfo memberInfo = MemberInfo.from(member);
        
        String accessToken = jwtProvider.createAccessToken(memberInfo);
        String refreshToken = jwtProvider.createRefreshToken(memberInfo);

        return new LoginResponse(accessToken, refreshToken, isExisting, MemberResponse.from(member));
    }

    private void validateTokenClaim(final AuthResponse response) {
        String iss = response.iss();
        if (!(ISSUER_GOOGLE.equals(iss) || ISSUER_GOOGLE_NO_SCHEME.equals(iss))) {
            throw new InvalidTokenException("Invalid issuer");
        }

        if (!googleAuthProperties.getClientId().equals(response.aud())) {
            throw new InvalidTokenException("Invalid audience");
        }

        long expEpoch = response.exp();
        if (Instant.ofEpochSecond(expEpoch).isBefore(Instant.now())) {
            throw new InvalidTokenException("Token expired");
        }
    }
}
