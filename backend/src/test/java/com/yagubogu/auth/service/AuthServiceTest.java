package com.yagubogu.auth.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.yagubogu.auth.config.AuthTestConfig;
import com.yagubogu.auth.domain.RefreshToken;
import com.yagubogu.auth.dto.LoginParam;
import com.yagubogu.auth.dto.v1.LoginResponse;
import com.yagubogu.auth.dto.v1.TokenResponse;
import com.yagubogu.auth.gateway.AuthGateway;
import com.yagubogu.auth.repository.RefreshTokenRepository;
import com.yagubogu.auth.support.AuthTokenProvider;
import com.yagubogu.auth.support.GoogleAuthValidator;
import com.yagubogu.global.config.JpaAuditingConfig;
import com.yagubogu.global.exception.UnAuthorizedException;
import com.yagubogu.member.domain.Member;
import com.yagubogu.member.service.MemberService;
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
@ActiveProfiles("integration")
@Import({AuthTestConfig.class, JpaAuditingConfig.class})
class AuthServiceTest {

    private AuthService authService;

    @Autowired
    private AuthGateway fakeAuthGateway;

    @Autowired
    private AuthTokenProvider authTokenProvider;

    @Autowired
    private GoogleAuthValidator googleAuthValidator;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private MemberFactory memberFactory;

    @Autowired
    private RefreshTokenFactory refreshTokenFactory;

    @Autowired
    private MemberService memberService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(fakeAuthGateway, authTokenProvider,
                List.of(googleAuthValidator), refreshTokenRepository, memberService, refreshTokenService);
    }

    @DisplayName("회원가입을 수행한다")
    @Test
    void login_register() {
        // given
        LoginParam loginParam = new LoginParam("ID_TOKEN");

        // when
        LoginResponse response = authService.login(loginParam);

        // then
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(response.accessToken()).isNotNull();
            softAssertions.assertThat(response.refreshToken()).isNotNull();
            softAssertions.assertThat(response.isNew()).isTrue();
        });
    }

    @DisplayName("로그인을 수행한다")
    @Test
    void login() {
        // given
        LoginParam loginParam = new LoginParam("ID_TOKEN");
        LoginResponse registerResponse = authService.login(loginParam);
        String expectedNickname = registerResponse.member().nickname();

        // when
        LoginResponse actual = authService.login(loginParam);

        // then
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(actual.accessToken()).isNotNull();
            softAssertions.assertThat(actual.refreshToken()).isNotNull();
            softAssertions.assertThat(actual.isNew()).isFalse();
            softAssertions.assertThat(actual.member().nickname()).isEqualTo(expectedNickname);
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

    @DisplayName("회원의 모든 Refresh Token을 revoke 한다")
    @Test
    void removeAllRefreshTokens_revokesAllTokensOfMember() {
        // given
        Member owner = memberFactory.save(MemberBuilder::build);
        Member other = memberFactory.save(MemberBuilder::build);

        // owner의 토큰(유효 2개 + 이미 revoke 1개)
        RefreshToken t1 = refreshTokenFactory.save(b -> b.member(owner));
        RefreshToken t2 = refreshTokenFactory.save(b -> b.member(owner));
        RefreshToken t3Revoked = refreshTokenFactory.save(b -> b.member(owner).isRevoked(true));

        // 다른 회원 토큰
        RefreshToken otherToken = refreshTokenFactory.save(b -> b.member(other));

        // when
        authService.removeAllRefreshTokens(owner.getId());

        // then
        List<RefreshToken> owners = refreshTokenRepository.findAllByMemberId(owner.getId());
        List<RefreshToken> others = refreshTokenRepository.findAllByMemberId(other.getId());

        assertSoftly(softly -> {
            softly.assertThat(owners).hasSize(3);
            softly.assertThat(owners).allMatch(RefreshToken::isRevoked);
            softly.assertThat(others).hasSize(1);
            softly.assertThat(others.getFirst().isRevoked())
                    .as("다른 회원 토큰은 revoke 되면 안 된다")
                    .isFalse();
        });
    }

    @DisplayName("토큰이 하나도 없어도 예외 없이 동작한다(멱등)")
    @Test
    void removeAllRefreshTokens_whenEmpty_isIdempotent() {
        // given
        Member member = memberFactory.save(MemberBuilder::build);

        // when
        authService.removeAllRefreshTokens(member.getId());
        authService.removeAllRefreshTokens(member.getId()); // 재호출(멱등성)

        // then
        List<RefreshToken> tokens = refreshTokenRepository.findAllByMemberId(member.getId());
        org.assertj.core.api.Assertions.assertThat(tokens).isEmpty();
    }

    @DisplayName("멱등성: 토큰이 있어도 두 번 호출해 항상 revoke 상태를 유지한다")
    @Test
    void removeAllRefreshTokens_withTokens_isIdempotent() {
        // given
        Member member = memberFactory.save(MemberBuilder::build);
        refreshTokenFactory.save(b -> b.member(member));
        refreshTokenFactory.save(b -> b.member(member));

        // when
        authService.removeAllRefreshTokens(member.getId());
        authService.removeAllRefreshTokens(member.getId()); // 재호출

        // then
        List<RefreshToken> tokens = refreshTokenRepository.findAllByMemberId(member.getId());
        org.assertj.core.api.Assertions.assertThat(tokens)
                .isNotEmpty()
                .allMatch(RefreshToken::isRevoked);
    }
}
