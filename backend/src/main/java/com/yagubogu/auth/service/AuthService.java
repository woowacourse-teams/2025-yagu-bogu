package com.yagubogu.auth.service;

import com.yagubogu.auth.config.AuthTokenProperties;
import com.yagubogu.auth.domain.RefreshToken;
import com.yagubogu.auth.dto.AuthResponse;
import com.yagubogu.auth.dto.LoginRequest;
import com.yagubogu.auth.dto.LoginResponse;
import com.yagubogu.auth.dto.LoginResponse.MemberResponse;
import com.yagubogu.auth.dto.LogoutRequest;
import com.yagubogu.auth.dto.MemberClaims;
import com.yagubogu.auth.dto.TokenResponse;
import com.yagubogu.auth.gateway.AuthGateway;
import com.yagubogu.auth.repository.RefreshTokenRepository;
import com.yagubogu.auth.support.AuthTokenProvider;
import com.yagubogu.auth.support.AuthValidator;
import com.yagubogu.global.exception.UnAuthorizedException;
import com.yagubogu.member.domain.Member;
import com.yagubogu.member.domain.OAuthProvider;
import com.yagubogu.member.domain.Role;
import com.yagubogu.member.repository.MemberRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class AuthService {

    private final MemberRepository memberRepository;
    private final AuthGateway authGateway;
    private final AuthTokenProvider authTokenProvider;
    private final List<AuthValidator<? extends AuthResponse>> authValidators;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthTokenProperties authTokenProperties;

    @Transactional
    public LoginResponse login(final LoginRequest request) {
        AuthResponse response = authGateway.validateToken(request);
        validateToken(response, OAuthProvider.GOOGLE);

        Optional<Member> memberOptional = memberRepository.findByOauthIdAndDeletedAtIsNull(response.oauthId());
        boolean isNew = memberOptional.isEmpty();
        Member member = findOrCreateMember(isNew, response, memberOptional);
        MemberClaims memberClaims = MemberClaims.from(member);

        String accessToken = authTokenProvider.createAccessToken(memberClaims);
        String refreshToken = generateRefreshToken(member);

        return new LoginResponse(accessToken, refreshToken, isNew, MemberResponse.from(member));
    }

    public MemberClaims makeMemberClaims(final String token) {
        authTokenProvider.validateAccessToken(token);
        Long memberId = authTokenProvider.getMemberIdByAccessToken(token);
        Role role = authTokenProvider.getRoleByAccessToken(token);

        return new MemberClaims(memberId, role);
    }

    @Transactional
    public TokenResponse refreshToken(final String refreshTokenId) {
        RefreshToken refreshToken = getPreviousValidRefreshToken(refreshTokenId);
        refreshToken.revoke();

        Member member = refreshToken.getMember();
        MemberClaims memberClaims = MemberClaims.from(member);

        String newAccessToken = authTokenProvider.createAccessToken(memberClaims);
        String newRefreshToken = generateRefreshToken(member);

        return new TokenResponse(newAccessToken, newRefreshToken);
    }

    @Transactional
    public void logout(final LogoutRequest request) {
        String previousRefreshToken = request.refreshToken();
        RefreshToken refreshToken = getPreviousValidRefreshToken(previousRefreshToken);
        refreshToken.revoke();
    }

    @Transactional
    public String generateRefreshToken(final Member member) {
        Instant expiresAt = calculateExpireAt();
        RefreshToken refreshToken = RefreshToken.generate(member, expiresAt);
        refreshTokenRepository.save(refreshToken);

        return refreshToken.getId();
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

    private RefreshToken getPreviousValidRefreshToken(final String refreshTokenId) {
        RefreshToken refreshToken = getRefreshToken(refreshTokenId);
        validateRefreshToken(refreshToken);

        return refreshToken;
    }

    private Instant calculateExpireAt() {
        long expiresIn = authTokenProperties.getRefreshToken().getExpiresIn();

        return Instant.now().plus(expiresIn, ChronoUnit.SECONDS);
    }

    private RefreshToken getRefreshToken(final String refreshTokenId) {
        return refreshTokenRepository.findById(refreshTokenId)
                .orElseThrow(() -> new UnAuthorizedException("Refresh token not exist"));
    }

    private void validateRefreshToken(final RefreshToken storedRefreshToken) {
        if (storedRefreshToken.isInValid()) {
            throw new UnAuthorizedException("Refresh token is invalid or expired");
        }
    }
}
