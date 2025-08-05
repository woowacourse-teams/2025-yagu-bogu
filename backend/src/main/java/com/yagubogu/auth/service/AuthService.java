package com.yagubogu.auth.service;

import com.yagubogu.auth.config.JwtProperties;
import com.yagubogu.auth.domain.RefreshToken;
import com.yagubogu.auth.dto.AuthResponse;
import com.yagubogu.auth.dto.CreateRefreshTokenResponse;
import com.yagubogu.auth.dto.LoginRequest;
import com.yagubogu.auth.dto.LoginResponse;
import com.yagubogu.auth.dto.LoginResponse.MemberResponse;
import com.yagubogu.auth.dto.MemberClaims;
import com.yagubogu.auth.gateway.AuthGateway;
import com.yagubogu.auth.repository.RefreshTokenRepository;
import com.yagubogu.auth.support.AuthValidator;
import com.yagubogu.auth.support.JwtProvider;
import com.yagubogu.global.exception.UnAuthorizedException;
import com.yagubogu.member.domain.Member;
import com.yagubogu.member.domain.OAuthProvider;
import com.yagubogu.member.domain.Role;
import com.yagubogu.member.repository.MemberRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class AuthService {

    private final MemberRepository memberRepository;
    private final AuthGateway authGateway;
    private final JwtProvider jwtProvider;
    private final List<AuthValidator<? extends AuthResponse>> authValidators;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties jwtProperties;

    public LoginResponse login(final LoginRequest request) {
        AuthResponse response = authGateway.validateToken(request);
        validateToken(response, OAuthProvider.GOOGLE);

        Optional<Member> memberOptional = memberRepository.findByOauthId(response.oauthId());
        boolean isNew = memberOptional.isEmpty();
        Member member = findOrCreateMember(isNew, response, memberOptional);
        MemberClaims memberClaims = MemberClaims.from(member);

        String accessToken = jwtProvider.createAccessToken(memberClaims);
        String refreshToken = generateRefreshToken(member);

        return new LoginResponse(accessToken, refreshToken, isNew, MemberResponse.from(member));
    }

    public MemberClaims makeMemberClaims(final String token) {
        jwtProvider.validateAccessToken(token);
        Long memberId = jwtProvider.getMemberIdByAccessToken(token);
        Role role = jwtProvider.getRoleByAccessToken(token);

        return new MemberClaims(memberId, role);
    }

    @Transactional
    public CreateRefreshTokenResponse refreshToken(final String refreshToken) {
        RefreshToken storedRefreshToken = getValidRefreshToken(refreshToken);
        storedRefreshToken.revoke();

        Member member = storedRefreshToken.getMember();
        MemberClaims memberClaims = MemberClaims.from(member);

        String newAccessToken = jwtProvider.createAccessToken(memberClaims);
        String newRefreshToken = generateRefreshToken(member);

        return new CreateRefreshTokenResponse(newAccessToken, newRefreshToken);
    }

    private Member findOrCreateMember(
            final boolean isNew,
            final AuthResponse response,
            final Optional<Member> memberOptional
    ) {
        if (isNew) {
            return memberRepository.save(response.toMember());
        }

        return memberOptional.get();
    }

    private void validateToken(
            final AuthResponse response,
            final OAuthProvider provider
    ) {
        authValidators.stream()
                .filter(v -> v.supports(provider))
                .findFirst()
                .ifPresentOrElse(
                        validator -> invokeValidator(validator, response),
                        () -> {
                            throw new UnsupportedOperationException("No validator for: " + provider);
                        }
                );
    }

    @SuppressWarnings("unchecked")
    private <T extends AuthResponse> void invokeValidator(
            final AuthValidator<?> validator,
            final AuthResponse response
    ) {
        ((AuthValidator<T>) validator).validate((T) response);
    }

    private RefreshToken getValidRefreshToken(final String refreshToken) {
        RefreshToken storedRefreshToken = refreshTokenRepository.findById(refreshToken)
                .orElseThrow(() -> new UnAuthorizedException("Refresh token not exist"));

        if (!storedRefreshToken.isValid()) {
            throw new UnAuthorizedException("Refresh token is invalid or expired");
        }

        return storedRefreshToken;
    }

    private String generateRefreshToken(final Member member) {
        String refreshToken = UUID.randomUUID().toString();
        Instant expiresAt = calculateExpireAt();
        refreshTokenRepository.save(new RefreshToken(refreshToken, member, expiresAt));

        return refreshToken;
    }

    private Instant calculateExpireAt() {
        long expireLength = jwtProperties.getRefreshToken().getExpireLength();

        return Instant.now().plus(expireLength, ChronoUnit.SECONDS);
    }
}
