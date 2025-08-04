package com.yagubogu.auth.service;

import com.yagubogu.auth.client.AuthGateway;
import com.yagubogu.auth.config.AuthTestConfig;
import com.yagubogu.auth.dto.LoginRequest;
import com.yagubogu.auth.dto.LoginResponse;
import com.yagubogu.auth.dto.LoginResponse.MemberResponse;
import com.yagubogu.member.repository.MemberRepository;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
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

    @BeforeEach
    void setUp() {
        authService = new AuthService(memberRepository, fakeAuthGateway, jwtProvider, List.of(googleAuthValidator));
    }

    @DisplayName("로그인을 수행한다")
    @Test
    void login() {
        // given
        LoginRequest loginRequest = new LoginRequest("ID_TOKEN");

        // when
        LoginResponse response = authService.login(loginRequest);

        // then
        SoftAssertions.assertSoftly(softAssertions -> {
            softAssertions.assertThat(response.accessToken()).isNotNull();
            softAssertions.assertThat(response.refreshToken()).isNotNull();
            softAssertions.assertThat(response.isNew()).isTrue();
            softAssertions.assertThat(response.member()).isEqualTo(new MemberResponse(1L, "test-user", "picture"));
        });
    }
}
