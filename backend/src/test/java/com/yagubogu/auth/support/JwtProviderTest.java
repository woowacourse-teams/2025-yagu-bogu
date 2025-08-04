package com.yagubogu.auth.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.yagubogu.auth.dto.MemberClaims;
import com.yagubogu.auth.config.JwtProperties;
import com.yagubogu.auth.config.JwtProperties.TokenProperties;
import com.yagubogu.global.exception.UnAuthorizedException;
import com.yagubogu.member.domain.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JwtProviderTest {

    private JwtProvider jwtProvider;

    @BeforeEach
    void setUp() {
        JwtProperties jwtProperties = new JwtProperties();
        TokenProperties accessToken = new TokenProperties();
        accessToken.setSecretKey("access-secret-key");
        accessToken.setExpireLength(90_000); // 15분

        TokenProperties refreshToken = new TokenProperties();
        refreshToken.setSecretKey("refresh-secret-key");
        refreshToken.setExpireLength(1_209_600_000); // 14일

        jwtProperties.setAccessToken(accessToken);
        jwtProperties.setRefreshToken(refreshToken);

        jwtProvider = new JwtProvider(jwtProperties);
    }

    @DisplayName("액세스 토큰을 생성한다")
    @Test
    void createAccessToken() {
        // given
        MemberClaims memberClaims = new MemberClaims(1L, Role.USER);

        // when
        String accessToken = jwtProvider.createAccessToken(memberClaims);

        // then
        assertThat(accessToken).isNotBlank();
    }

    @DisplayName("리프레시 토큰을 생성한다")
    @Test
    void createRefreshToken() {
        // given
        MemberClaims memberClaims = new MemberClaims(1L, Role.USER);

        // when
        String refreshToken = jwtProvider.createRefreshToken(memberClaims);

        // then
        assertThat(refreshToken).isNotBlank();
    }

    @DisplayName("예외: 만료된 액세스 토큰이면 예외를 발생시킨다")
    @Test
    void validateAccessToken_expired() {
        // given
        MemberClaims memberClaims = new MemberClaims(1L, Role.USER);

        JwtProperties jwtProperties = new JwtProperties();
        TokenProperties shortExpAccess = new TokenProperties();
        shortExpAccess.setSecretKey("access-secret-key");
        shortExpAccess.setExpireLength(1);

        TokenProperties shortExpRefresh = new TokenProperties();
        shortExpRefresh.setSecretKey("refresh-secret-key");
        shortExpRefresh.setExpireLength(1000000);

        jwtProperties.setAccessToken(shortExpAccess);
        jwtProperties.setRefreshToken(shortExpRefresh);

        JwtProvider expiredTokenProvider = new JwtProvider(jwtProperties);

        // when
        String expiredToken = expiredTokenProvider.createAccessToken(memberClaims);
        try {
            Thread.sleep(1100);
        } catch (InterruptedException ignored) {
        }

        // then
        assertThatThrownBy(() -> expiredTokenProvider.validateAccessToken(expiredToken))
                .isInstanceOf(UnAuthorizedException.class)
                .hasMessageContaining("Expired token");
    }

    @DisplayName("예외: 유효하지 않은 액세스 토큰이면 예외를 발생시킨다")
    @Test
    void validateAccessToken_verification() {
        // given
        String invalidToken = "invalid.token";

        // when & then
        assertThatThrownBy(() -> jwtProvider.validateAccessToken(invalidToken))
                .isInstanceOf(UnAuthorizedException.class)
                .hasMessageContaining("Invalid token");
    }

    @DisplayName("액세스 토큰을 통해 memberId를 반환한다")
    @Test
    void getMemberIdByAccessToken() {
        // given
        MemberClaims memberClaims = new MemberClaims(1L, Role.USER);
        String accessToken = jwtProvider.createAccessToken(memberClaims);

        // when
        Long memberId = jwtProvider.getMemberIdByAccessToken(accessToken);

        // then
        assertThat(memberId).isEqualTo(1L);
    }

    @DisplayName("액세스 토큰을 통해 role을 반환한다")
    @Test
    void getRoleByAccessToken() {
        // given
        MemberClaims memberClaims = new MemberClaims(1L, Role.USER);
        String accessToken = jwtProvider.createAccessToken(memberClaims);

        // when
        Role role = jwtProvider.getRoleByAccessToken(accessToken);

        // then
        assertThat(role).isEqualTo(Role.USER);
    }
}
