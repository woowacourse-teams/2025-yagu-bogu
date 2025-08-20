package com.yagubogu.auth.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.yagubogu.auth.config.AuthTestConfig;
import com.yagubogu.auth.config.AuthTokenProperties;
import com.yagubogu.auth.domain.RefreshToken;
import com.yagubogu.auth.dto.LoginRequest;
import com.yagubogu.auth.dto.LoginResponse;
import com.yagubogu.auth.dto.TokenResponse;
import com.yagubogu.auth.gateway.AuthGateway;
import com.yagubogu.auth.repository.RefreshTokenRepository;
import com.yagubogu.auth.support.AuthTokenProvider;
import com.yagubogu.auth.support.GoogleAuthValidator;
import com.yagubogu.global.exception.UnAuthorizedException;
import com.yagubogu.member.domain.Member;
import com.yagubogu.member.repository.MemberRepository;
import com.yagubogu.support.TestFixture;
import com.yagubogu.support.member.MemberBuilder;
import com.yagubogu.support.member.MemberFactory;
import com.yagubogu.support.refreshtoken.RefreshTokenFactory;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@Import(AuthTestConfig.class)
@ActiveProfiles("integration")
class AuthServiceTest {

    private AuthService authService;

    @Autowired
    private AuthGateway fakeAuthGateway;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private AuthTokenProvider authTokenProvider;

    @Autowired
    private GoogleAuthValidator googleAuthValidator;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private AuthTokenProperties authTokenProperties;

    @Autowired
    private MemberFactory memberFactory;

    @Autowired
    private RefreshTokenFactory refreshTokenFactory;

    @BeforeEach
    void setUp() {
        authService = new AuthService(memberRepository, fakeAuthGateway, authTokenProvider,
                List.of(googleAuthValidator), refreshTokenRepository, authTokenProperties);
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
        Member member = memberFactory.save(MemberBuilder::build);
        RefreshToken refreshToken = refreshTokenFactory.save(builder -> builder.member(member));
        String refreshTokenId = refreshToken.getId();

        // when
        TokenResponse response = authService.refreshToken(refreshTokenId);

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
                .isExactlyInstanceOf(UnAuthorizedException.class)
                .hasMessage("Refresh token not exist");
    }

    @DisplayName("예외: refresh token이 만료되었으면 예외가 발생한다")
    @Test
    void refresh_Token_tokenExpired() {
        // given
        Instant expiresAt = Instant.now().minusSeconds(10);
        Member member = memberFactory.save(MemberBuilder::build);
        RefreshToken expiredToken = refreshTokenFactory.save(
                builder -> builder
                        .member(member)
                        .expiresAt(expiresAt)
        );
        String expiredTokenId = expiredToken.getId();

        // when & then
        assertThatThrownBy(() -> authService.refreshToken(expiredTokenId))
                .isExactlyInstanceOf(UnAuthorizedException.class)
                .hasMessage("Refresh token is invalid or expired");
    }

    @DisplayName("예외: refresh token이 만료되었거나 폐기되었으면 예외가 발생한다")
    @Test
    void refresh_Token_tokenRevoked() {
        // given
        Member member = memberFactory.save(MemberBuilder::build);
        RefreshToken revokedToken = refreshTokenFactory.save(
                builder -> builder.member(member)
                        .expiresAt(TestFixture.getAfter60Minutes())
                        .isRevoked(true)
        );
        String revokedTokenId = revokedToken.getId();

        // when & then
        assertThatThrownBy(() -> authService.refreshToken(revokedTokenId))
                .isExactlyInstanceOf(UnAuthorizedException.class)
                .hasMessage("Refresh token is invalid or expired");
    }
}
