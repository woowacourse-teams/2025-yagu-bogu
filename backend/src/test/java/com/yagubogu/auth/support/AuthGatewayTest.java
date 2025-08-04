package com.yagubogu.auth.support;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.yagubogu.auth.gateway.AuthGateway;
import com.yagubogu.auth.dto.GoogleAuthResponse;
import com.yagubogu.auth.dto.LoginRequest;
import com.yagubogu.auth.dto.LoginResponse;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.Instant;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@TestPropertySource(properties = {
        "spring.sql.init.data-locations=classpath:test-data.sql"
})
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class AuthGatewayTest {

    @LocalServerPort
    private int port;

    @MockitoBean
    private AuthGateway authGateway;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @DisplayName("로그인을 수행한다")
    @Test
    void login() {
        // given
        GoogleAuthResponse googleAuthResponse = new GoogleAuthResponse("accounts.google.com", "sub-test-unique-01",
                "azp",
                "this-is-client-id",
                111L, Instant.now().plusSeconds(3000).getEpochSecond(), "email", true, "name",
                "picture", "givenName", "familyName", "ko");
        when(authGateway.validateToken(any())).thenReturn(googleAuthResponse);

        // when
        LoginResponse loginResponse = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(new LoginRequest("ID_TOKEN"))
                .when().post("/api/auth/login")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(LoginResponse.class);

        // then
        SoftAssertions.assertSoftly(softAssertions -> {
            softAssertions.assertThat(loginResponse.accessToken()).isNotBlank();
            softAssertions.assertThat(loginResponse.refreshToken()).isNotBlank();
            softAssertions.assertThat(loginResponse.isNew()).isTrue();
        });
    }
}
