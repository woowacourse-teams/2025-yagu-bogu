package com.yagubogu.auth.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.yagubogu.auth.config.AuthTestConfig;
import com.yagubogu.auth.config.JwtProperties;
import com.yagubogu.auth.domain.RefreshToken;
import com.yagubogu.auth.dto.CreateTokenResponse;
import com.yagubogu.auth.dto.LoginRequest;
import com.yagubogu.auth.dto.LoginResponse;
import com.yagubogu.auth.gateway.AuthGateway;
import com.yagubogu.auth.repository.RefreshTokenRepository;
import com.yagubogu.auth.support.GoogleAuthValidator;
import com.yagubogu.auth.support.JwtProvider;
import com.yagubogu.fixture.TestFixture;
import com.yagubogu.global.exception.UnAuthorizedException;
import com.yagubogu.member.domain.Member;
import com.yagubogu.member.repository.MemberRepository;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.sql.init.data-locations=classpath:test-data.sql"
})
@Import(AuthTestConfig.class)
class AuthServiceTest {

    private AuthService authService;

    @Autowired
    private AuthGateway fakeAuthGateway;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private GoogleAuthValidator googleAuthValidator;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private JwtProperties jwtProperties;

    @BeforeEach
    void setUp() {
        authService = new AuthService(memberRepository, fakeAuthGateway, jwtProvider, List.of(googleAuthValidator),
                refreshTokenRepository, jwtProperties);
    }

    @DisplayName("로그인을 수행한다")
    @Test
    void login() {
        // given
        LoginRequest loginRequest = new LoginRequest("ID_TOKEN");
        String expectedNickname = "test-user";

        // when
        LoginResponse response = authService.login(loginRequest);

        // then
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(response.accessToken()).isNotNull();
            softAssertions.assertThat(response.refreshToken()).isNotNull();
            softAssertions.assertThat(response.isNew()).isTrue();
            softAssertions.assertThat(response.member().nickname()).isEqualTo(expectedNickname);
        });
    }

    @DisplayName("토큰을 갱신한다")
    @Test
    void refreshToken() {
        // given
        String refreshTokenId = "id";
        Member member = memberRepository.findById(1L).orElseThrow();
        RefreshToken refreshToken = new RefreshToken(refreshTokenId, member, TestFixture.getAfter60Minutes());
        refreshTokenRepository.save(refreshToken);

        // when
        CreateTokenResponse response = authService.refreshToken(refreshTokenId);

        // then
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(response.accessToken()).isNotEmpty();
            softAssertions.assertThat(response.refreshToken()).isNotEmpty();
            softAssertions.assertThat(response.refreshToken()).isNotEqualTo(refreshTokenId);
        });
    }

    @DisplayName("예외: refresh token이 존재하지 않으면 예외가 발생한다")
    @Test
    void refresh_Token_tokenNotFound() {
        // given
        String nonExistToken = "non-exist-token";

        // when & then
        assertThatThrownBy(() -> authService.refreshToken(nonExistToken))
                .isInstanceOf(UnAuthorizedException.class)
                .hasMessageContaining("Refresh token not exist");
    }

    @DisplayName("예외: refresh token이 만료되었거나 폐기되었으면 예외가 발생한다")
    @Test
    void refresh_Token_tokenInvalid() {
        // given
        String refreshTokenId = "expired-token";
        Instant expiresAt = Instant.now().minusSeconds(10);
        Member member = memberRepository.findById(1L).orElseThrow();
        RefreshToken expiredToken = new RefreshToken(refreshTokenId, member, expiresAt);
        refreshTokenRepository.save(expiredToken);

        // when & then
        assertThatThrownBy(() -> authService.refreshToken(refreshTokenId))
                .isInstanceOf(UnAuthorizedException.class)
                .hasMessageContaining("Refresh token is invalid or expired");
    }
}
