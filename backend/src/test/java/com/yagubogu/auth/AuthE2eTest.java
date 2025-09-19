package com.yagubogu.auth;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.yagubogu.auth.config.AuthTestConfig;
import com.yagubogu.auth.domain.RefreshToken;
import com.yagubogu.auth.dto.LoginRequest;
import com.yagubogu.auth.dto.LoginResponse;
import com.yagubogu.auth.dto.LogoutRequest;
import com.yagubogu.auth.dto.TokenRequest;
import com.yagubogu.auth.dto.TokenResponse;
import com.yagubogu.global.config.JpaAuditingConfig;
import com.yagubogu.member.domain.Member;
import com.yagubogu.support.E2eTestBase;
import com.yagubogu.support.TestFixture;
import com.yagubogu.support.TestSupport;
import com.yagubogu.support.member.MemberBuilder;
import com.yagubogu.support.member.MemberFactory;
import com.yagubogu.support.refreshtoken.RefreshTokenFactory;
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

@Import({AuthTestConfig.class, JpaAuditingConfig.class})
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class AuthE2eTest extends E2eTestBase {

    private static final String BEARER = "Bearer ";
    private static final String ID_TOKEN = "ID_TOKEN";

    @LocalServerPort
    private int port;

    @Autowired
    private MemberFactory memberFactory;

    @Autowired
    private RefreshTokenFactory refreshTokenFactory;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @DisplayName("회원가입을 수행한다")
    @Test
    void login_register() {
        // given & when
        LoginResponse actual = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(new LoginRequest(ID_TOKEN))
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
        });
    }

    @DisplayName("로그인을 수행한다")
    @Test
    void login() {
        // given
        LoginResponse registerResponse = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(new LoginRequest(ID_TOKEN))
                .when().post("/api/auth/login")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(LoginResponse.class);
        String expectedStringName = registerResponse.member().nickname();

        // when
        LoginResponse actual = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(new LoginRequest(ID_TOKEN))
                .when().post("/api/auth/login")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(LoginResponse.class);

        // then
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(actual.accessToken()).isNotNull();
            softAssertions.assertThat(actual.refreshToken()).isNotNull();
            softAssertions.assertThat(actual.isNew()).isFalse();
            softAssertions.assertThat(actual.member().nickname()).isEqualTo(expectedStringName);
        });
    }

    @DisplayName("토큰을 갱신한다")
    @Test
    void refresh() {
        // given
        LoginResponse loginResponse = TestSupport.loginResponse(ID_TOKEN);
        String refreshToken = loginResponse.refreshToken();

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

    @DisplayName("예외: refresh token이 만료되었으면 예외가 발생한다")
    @Test
    void refresh_tokenExpired() {
        // given
        Member member = memberFactory.save(MemberBuilder::build);
        RefreshToken expiredRefreshToken = refreshTokenFactory.save(builder -> builder.member(member)
                .expiresAt(Instant.now().minusSeconds(1)));
        String expiredRefreshTokenId = expiredRefreshToken.getId();

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(new TokenRequest(expiredRefreshTokenId))
                .when().post("/api/auth/refresh")
                .then().log().all()
                .statusCode(401);
    }

    @DisplayName("예외: refresh token이 폐기되었으면 예외가 발생한다")
    @Test
    void refresh_tokenRevoked() {
        // given
        Member member = memberFactory.save(MemberBuilder::build);
        RefreshToken revokedRefreshToken = refreshTokenFactory.save(
                builder -> builder
                        .member(member)
                        .expiresAt(TestFixture.getAfter60Minutes())
                        .isRevoked(true)
        );
        String revokedRefreshTokenId = revokedRefreshToken.getId();

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(new TokenRequest(revokedRefreshTokenId))
                .when().post("/api/auth/refresh")
                .then().log().all()
                .statusCode(401);
    }

    @DisplayName("로그아웃한다")
    @Test
    void logout() {
        // given
        LoginResponse loginResponse = TestSupport.loginResponse(ID_TOKEN);
        String accessToken = BEARER + loginResponse.accessToken();
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
        String accessToken = TestSupport.getAccessTokenByMemberId(ID_TOKEN);
        String nonExistTokenId = "non-exist-token";

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .body(new LogoutRequest(nonExistTokenId))
                .when().post("/api/auth/logout")
                .then().log().all()
                .statusCode(401);
    }

    @DisplayName("예외: refresh token이 만료되었으면 예외가 발생한다")
    @Test
    void logout_tokenExpired() {
        // given
        String accessToken = TestSupport.getAccessTokenByMemberId(ID_TOKEN);
        Member member = memberFactory.save(MemberBuilder::build);

        RefreshToken refreshToken = refreshTokenFactory.save(
                builder -> builder
                        .expiresAt(Instant.now().minusSeconds(1))
                        .member(member)
        );
        String expiredTokenId = refreshToken.getId();

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .body(new LogoutRequest(expiredTokenId))
                .when().post("/api/auth/logout")
                .then().log().all()
                .statusCode(401);
    }

    @DisplayName("예외: refresh token이 폐기되었으면 예외가 발생한다")
    @Test
    void logout_tokenRevoked() {
        // given
        String accessToken = TestSupport.getAccessTokenByMemberId(ID_TOKEN);
        Member member = memberFactory.save(MemberBuilder::build);

        RefreshToken refreshToken = refreshTokenFactory.save(
                builder -> builder
                        .expiresAt(TestFixture.getAfter60Minutes())
                        .member(member)
                        .isRevoked(true)
        );
        String revokedTokenId = refreshToken.getId();

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .body(new LogoutRequest(revokedTokenId))
                .when().post("/api/auth/logout")
                .then().log().all()
                .statusCode(401);
    }
}
