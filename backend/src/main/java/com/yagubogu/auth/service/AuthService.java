package com.yagubogu.auth.service;

import com.yagubogu.auth.domain.RefreshToken;
import com.yagubogu.auth.dto.AuthParam;
import com.yagubogu.auth.dto.LoginParam;
import com.yagubogu.auth.dto.LogoutParam;
import com.yagubogu.auth.dto.MemberClaims;
import com.yagubogu.auth.dto.v1.LoginResponse;
import com.yagubogu.auth.dto.v1.LoginResponse.MemberResponse;
import com.yagubogu.auth.dto.v1.TokenResponse;
import com.yagubogu.auth.gateway.AuthGateway;
import com.yagubogu.auth.repository.RefreshTokenRepository;
import com.yagubogu.auth.support.AuthTokenProvider;
import com.yagubogu.auth.support.AuthValidator;
import com.yagubogu.global.exception.UnAuthorizedException;
import com.yagubogu.member.domain.Member;
import com.yagubogu.member.domain.OAuthProvider;
import com.yagubogu.member.domain.Role;
import com.yagubogu.member.dto.MemberFindResult;
import com.yagubogu.member.service.MemberService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class AuthService {

    private final AuthGateway authGateway;
    private final AuthTokenProvider authTokenProvider;
    private final List<AuthValidator<? extends AuthParam>> authValidators;
    private final RefreshTokenRepository refreshTokenRepository;
    private final MemberService memberService;
    private final RefreshTokenService refreshTokenService;

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public LoginResponse login(final LoginParam request) {
        AuthParam response = authGateway.validateToken(request);
        validateToken(response, OAuthProvider.GOOGLE);

        MemberFindResult memberFindResult = memberService.findMember(response);
        Member member = memberFindResult.member();
        boolean isNew = memberFindResult.isNew();

        String accessToken = authTokenProvider.issueAccessToken(MemberClaims.from(member));
        String refreshToken = refreshTokenService.issue(member);

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

        String newAccessToken = authTokenProvider.issueAccessToken(memberClaims);
        String newRefreshToken = refreshTokenService.issue(member);

        return new TokenResponse(newAccessToken, newRefreshToken);
    }

    @Transactional
    public void logout(final LogoutParam request) {
        String previousRefreshToken = request.refreshToken();
        RefreshToken refreshToken = getPreviousValidRefreshToken(previousRefreshToken);
        refreshToken.revoke();
    }

    @Transactional
    public void removeAllRefreshTokens(final Long memberId) {
        List<RefreshToken> refreshTokens = refreshTokenRepository.findAllByMemberId(memberId);
        for (RefreshToken refreshToken : refreshTokens) {
            refreshToken.revoke();
        }
    }

    private void validateToken(
            final AuthParam response,
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
    private <T extends AuthParam> void invokeValidator(
            final AuthValidator<?> validator,
            final AuthParam response
    ) {
        ((AuthValidator<T>) validator).validate((T) response);
    }

    private RefreshToken getPreviousValidRefreshToken(final String refreshTokenId) {
        RefreshToken refreshToken = getRefreshToken(refreshTokenId);
        validateRefreshToken(refreshToken);

        return refreshToken;
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
