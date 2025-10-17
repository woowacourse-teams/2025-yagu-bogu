package com.yagubogu.auth.support;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.yagubogu.auth.config.AuthTestConfig;
import com.yagubogu.auth.dto.LoginParam;
import com.yagubogu.auth.dto.v1.LoginResponse;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

@Import(AuthTestConfig.class)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class AuthGatewayTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @DisplayName("로그인을 수행한다")
    @Test
    void login() {
        // given & when
        LoginResponse loginResponse = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(new LoginParam("ID_TOKEN"))
                .when().post("/api/auth/login")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(LoginResponse.class);

        // then
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(loginResponse.accessToken()).isNotBlank();
            softAssertions.assertThat(loginResponse.refreshToken()).isNotBlank();
            softAssertions.assertThat(loginResponse.isNew()).isTrue();
        });
    }
}
