package com.yagubogu.auth.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.yagubogu.auth.config.AuthTokenProperties;
import com.yagubogu.auth.config.AuthTokenProperties.TokenProperties;
import com.yagubogu.auth.dto.MemberClaims;
import com.yagubogu.global.exception.UnAuthorizedException;
import com.yagubogu.member.domain.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AuthTokenProviderTest {

    private AuthTokenProvider authTokenProvider;

    @BeforeEach
    void setUp() {
        AuthTokenProperties authTokenProperties = new AuthTokenProperties();
        TokenProperties accessToken = new TokenProperties();
        accessToken.setSecretKey("access-secret-key");
        accessToken.setExpiresIn(90_000); // 15분

        TokenProperties refreshToken = new TokenProperties();
        refreshToken.setSecretKey("refresh-secret-key");
        refreshToken.setExpiresIn(1_209_600_000); // 14일

        authTokenProperties.setAccessToken(accessToken);
        authTokenProperties.setRefreshToken(refreshToken);

        authTokenProvider = new AuthTokenProvider(authTokenProperties);
    }

    @DisplayName("액세스 토큰을 생성한다")
    @Test
    void issueAccessToken() {
        // given
        MemberClaims memberClaims = new MemberClaims(1L, Role.USER);

        // when
        String accessToken = authTokenProvider.issueAccessToken(memberClaims);

        // then
        assertThat(accessToken).isNotBlank();
    }

    @DisplayName("리프레시 토큰을 생성한다")
    @Test
    void createRefreshToken() {
        // given
        MemberClaims memberClaims = new MemberClaims(1L, Role.USER);

        // when
        String refreshToken = authTokenProvider.createRefreshToken(memberClaims);

        // then
        assertThat(refreshToken).isNotBlank();
    }

    @DisplayName("예외: 만료된 액세스 토큰이면 예외를 발생시킨다")
    @Test
    void validateAccessToken_expired() {
        // given
        MemberClaims memberClaims = new MemberClaims(1L, Role.USER);

        AuthTokenProperties authTokenProperties = new AuthTokenProperties();
        TokenProperties shortExpAccess = new TokenProperties();
        shortExpAccess.setSecretKey("access-secret-key");
        shortExpAccess.setExpiresIn(1);

        TokenProperties shortExpRefresh = new TokenProperties();
        shortExpRefresh.setSecretKey("refresh-secret-key");
        shortExpRefresh.setExpiresIn(1000000);

        authTokenProperties.setAccessToken(shortExpAccess);
        authTokenProperties.setRefreshToken(shortExpRefresh);

        AuthTokenProvider expiredTokenProvider = new AuthTokenProvider(authTokenProperties);

        // when
        String expiredToken = expiredTokenProvider.issueAccessToken(memberClaims);
        try {
            Thread.sleep(1100);
        } catch (InterruptedException ignored) {
        }

        // then
        assertThatThrownBy(() -> expiredTokenProvider.validateAccessToken(expiredToken))
                .isExactlyInstanceOf(UnAuthorizedException.class)
                .hasMessage("Expired token");
    }

    @DisplayName("예외: 유효하지 않은 액세스 토큰이면 예외를 발생시킨다")
    @Test
    void validateAccessToken_verification() {
        // given
        String invalidToken = "invalid.token";

        // when & then
        assertThatThrownBy(() -> authTokenProvider.validateAccessToken(invalidToken))
                .isExactlyInstanceOf(UnAuthorizedException.class)
                .hasMessage("Invalid token");
    }

    @DisplayName("액세스 토큰을 통해 memberId를 반환한다")
    @Test
    void getMemberIdByAccessToken() {
        // given
        MemberClaims memberClaims = new MemberClaims(1L, Role.USER);
        String accessToken = authTokenProvider.issueAccessToken(memberClaims);

        // when
        Long memberId = authTokenProvider.getMemberIdByAccessToken(accessToken);

        // then
        assertThat(memberId).isEqualTo(1L);
    }

    @DisplayName("액세스 토큰을 통해 role을 반환한다")
    @Test
    void getRoleByAccessToken() {
        // given
        MemberClaims memberClaims = new MemberClaims(1L, Role.USER);
        String accessToken = authTokenProvider.issueAccessToken(memberClaims);

        // when
        Role role = authTokenProvider.getRoleByAccessToken(accessToken);

        // then
        assertThat(role).isEqualTo(Role.USER);
    }
}
