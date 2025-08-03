package com.yagubogu.auth.token;

import static org.assertj.core.api.Assertions.assertThat;

import com.yagubogu.auth.dto.MemberClaims;
import com.yagubogu.global.config.JwtProperties;
import com.yagubogu.global.config.JwtProperties.TokenProperties;
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
        // Given
        MemberClaims memberClaims = new MemberClaims(1L, Role.USER);

        // When
        String accessToken = jwtProvider.createAccessToken(memberClaims);

        // Then
        assertThat(accessToken).isNotBlank();
    }

    @DisplayName("리프레시 토큰을 생성한다")
    @Test
    void createRefreshToken() {
        // Given
        MemberClaims memberClaims = new MemberClaims(1L, Role.USER);

        // When
        String refreshToken = jwtProvider.createRefreshToken(memberClaims);

        // Then
        assertThat(refreshToken).isNotBlank();
    }

    @DisplayName("예외: 유효하지 않은 액세스 토큰이면 false를 발생한다")
    @Test
    void isInvalidAccessToken() {
        // Given
        MemberClaims memberClaims = new MemberClaims(1L, Role.USER);

        JwtProperties jwtProperties = new JwtProperties();
        TokenProperties shortExpAccess = new TokenProperties();
        shortExpAccess.setSecretKey("access-secret-key");
        shortExpAccess.setExpireLength(1);

        TokenProperties shortExpRefresh = new TokenProperties();
        shortExpAccess.setSecretKey("refresh-secret-key");
        shortExpAccess.setExpireLength(1000000);

        jwtProperties.setAccessToken(shortExpAccess);
        jwtProperties.setRefreshToken(shortExpRefresh);

        JwtProvider expiredTokenProvider = new JwtProvider(jwtProperties);

        // When
        String expiredToken = expiredTokenProvider.createAccessToken(memberClaims);
        try {
            Thread.sleep(5);
        } catch (InterruptedException ignored) {
        }

        // Then
        assertThat(expiredTokenProvider.isInvalidAccessToken(expiredToken)).isFalse();
    }

    @DisplayName("액세스 토큰을 통해 memberId를 반환한다")
    @Test
    void getMemberIdByAccessToken() {
        // Given
        MemberClaims memberClaims = new MemberClaims(1L, Role.USER);
        String accessToken = jwtProvider.createAccessToken(memberClaims);

        // When
        Long memberId = jwtProvider.getMemberIdByAccessToken(accessToken);

        // Then
        assertThat(memberId).isEqualTo(1L);
    }

    @DisplayName("액세스 토큰을 통해 role을 반환한다")
    @Test
    void getRoleByAccessToken() {
        // Given
        MemberClaims memberClaims = new MemberClaims(1L, Role.USER);
        String accessToken = jwtProvider.createAccessToken(memberClaims);

        // When
        Role role = jwtProvider.getRoleByAccessToken(accessToken);

        // Then
        assertThat(role).isEqualTo(Role.USER);
    }
}
