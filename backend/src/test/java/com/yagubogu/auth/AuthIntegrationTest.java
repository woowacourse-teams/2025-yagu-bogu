package com.yagubogu.auth;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.yagubogu.auth.config.AuthTestConfig;
import com.yagubogu.auth.domain.RefreshToken;
import com.yagubogu.auth.dto.LoginRequest;
import com.yagubogu.auth.dto.LoginResponse;
import com.yagubogu.auth.dto.LogoutRequest;
import com.yagubogu.auth.dto.TokenRequest;
import com.yagubogu.auth.dto.TokenResponse;
import com.yagubogu.auth.repository.RefreshTokenRepository;
import com.yagubogu.fixture.TestFixture;
import com.yagubogu.fixture.TestSupport;
import com.yagubogu.member.domain.Member;
import com.yagubogu.member.repository.MemberRepository;
import com.yagubogu.team.domain.Team;
import com.yagubogu.team.repository.TeamRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.TestPropertySource;

@Import(AuthTestConfig.class)
@TestPropertySource(properties = {
        "spring.sql.init.data-locations=classpath:test-data.sql"
})
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class AuthIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private TeamRepository teamRepository;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @DisplayName("로그인한다")
    @Test
    void login() {
        // given & when
        LoginResponse actual = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(new LoginRequest("id_token"))
                .when().post("/api/auth/login")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(LoginResponse.class);

        // then
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(actual.accessToken()).isNotNull();
            softAssertions.assertThat(actual.refreshToken()).isNotNull();
            softAssertions.assertThat(actual.isNew()).isTrue();
            softAssertions.assertThat(actual.member().nickname()).isEqualTo("test-user");
        });
    }

    @DisplayName("토큰을 갱신한다")
    @Test
    void refresh() {
        // given
        String refreshToken = TestSupport.getRefreshToken("id_token");

        // when
        TokenResponse actual = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(new TokenRequest(refreshToken))
                .when().post("/api/auth/refresh")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(TokenResponse.class);

        // then
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(actual.accessToken()).isNotBlank();
            softAssertions.assertThat(actual.refreshToken()).isNotBlank();
            softAssertions.assertThat(actual.refreshToken()).isNotEqualTo(refreshToken);
        });
    }

    @DisplayName("예외: refresh token이 존재하지 않으면 예외가 발생한다")
    @Test
    void refresh_tokenNotFound() {
        // given
        String nonExistToken = "non-exist-token";

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(new TokenRequest(nonExistToken))
                .when().post("/api/auth/refresh")
                .then().log().all()
                .statusCode(401);
    }

    @DisplayName("예외: refresh token이 만료되었거나 폐기되었으면 예외가 발생한다")
    @Test
    void refresh_tokenInvalid() {
        // given
        String expiredToken = "expired-token";
        Team team = teamRepository.save(TestFixture.getTeam());
        Member member = memberRepository.save(TestFixture.getUser(team));

        RefreshToken refreshToken = new RefreshToken(
                expiredToken,
                member,
                Instant.now().minusSeconds(1)
        );
        refreshToken.revoke();
        refreshTokenRepository.save(refreshToken);

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(new TokenRequest(expiredToken))
                .when().post("/api/auth/refresh")
                .then().log().all()
                .statusCode(401);
    }

    @DisplayName("로그아웃한다")
    @Test
    void logout() {
        // given
        String idToken = "id_token";
        LoginResponse loginResponse = TestSupport.loginResponse(idToken);
        String accessToken = "Bearer " + loginResponse.accessToken();
        String refreshTokenId = loginResponse.refreshToken();

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .body(new LogoutRequest(refreshTokenId))
                .when().post("/api/auth/logout")
                .then().log().all()
                .statusCode(204);
    }

    @DisplayName("예외: refresh token이 존재하지 않으면 예외가 발생한다")
    @Test
    void logout_tokenNotFound() {
        // given
        String nonExistTokenId = "non-exist-token";

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(new LogoutRequest(nonExistTokenId))
                .when().post("/api/auth/logout")
                .then().log().all()
                .statusCode(401);
    }

    @DisplayName("예외: refresh token이 만료되었거나 폐기되었으면 예외가 발생한다")
    @Test
    void logout_tokenInvalid() {
        // given
        String expiredTokenId = "expired-token";
        Team team = teamRepository.save(TestFixture.getTeam());
        Member member = memberRepository.save(TestFixture.getUser(team));

        RefreshToken refreshToken = new RefreshToken(
                expiredTokenId,
                member,
                Instant.now().minusSeconds(1)
        );
        refreshToken.revoke();
        refreshTokenRepository.save(refreshToken);

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(new LogoutRequest(expiredTokenId))
                .when().post("/api/auth/logout")
                .then().log().all()
                .statusCode(401);
    }
}
