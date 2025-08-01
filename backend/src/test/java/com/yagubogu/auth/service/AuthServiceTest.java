package com.yagubogu.auth.service;

import static org.mockito.Mockito.when;

import com.yagubogu.auth.client.AuthGateway;
import com.yagubogu.auth.dto.AuthResponse;
import com.yagubogu.auth.dto.LoginRequest;
import com.yagubogu.auth.dto.LoginResponse;
import com.yagubogu.auth.dto.LoginResponse.MemberResponse;
import com.yagubogu.auth.token.JwtProvider;
import com.yagubogu.global.config.GoogleAuthProperties;
import com.yagubogu.global.config.TestConfig;
import com.yagubogu.member.repository.MemberRepository;
import java.time.Instant;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@DataJpaTest
@Import(TestConfig.class)
class AuthServiceTest {

    private AuthService authService;

    @MockitoBean
    private AuthGateway authGateway;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private GoogleAuthProperties googleAuthProperties;

    @BeforeEach
    void setUp() {
        authService = new AuthService(memberRepository, authGateway, jwtProvider, googleAuthProperties);
    }

    @Test
    @DisplayName("로그인을 수행한다")
    void login() {
        // given
        LoginRequest loginRequest = new LoginRequest("ID_TOKEN");
        AuthResponse authResponse = new AuthResponse("accounts.google.com", "sub-test-unique-01", "azp",
                "this-is-client-id",
                111L, Instant.now().plusSeconds(3000).getEpochSecond(), "email", true, "name",
                "picture", "givenName", "familyName", "ko");

        when(authGateway.validateToken(loginRequest)).thenReturn(authResponse);

        // when
        LoginResponse response = authService.login(loginRequest);

        // then
        SoftAssertions.assertSoftly(softAssertions -> {
            softAssertions.assertThat(response.accessToken()).isNotNull();
            softAssertions.assertThat(response.refreshToken()).isNotNull();
            softAssertions.assertThat(response.isNew()).isFalse();
            softAssertions.assertThat(response.user()).isEqualTo(new MemberResponse(1L, "name", "picture"));
        });
    }
}
