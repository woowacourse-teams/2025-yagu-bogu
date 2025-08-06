package com.yagubogu.member;

import static org.assertj.core.api.Assertions.assertThat;

import com.yagubogu.auth.config.AuthTestConfig;
import com.yagubogu.auth.support.AuthTokenProvider;
import com.yagubogu.fixture.TestSupport;
import com.yagubogu.member.dto.MemberFavoriteResponse;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
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
public class MemberIntegrationTest {

    private static final String ID_TOKEN = "ID_TOKEN";

    @LocalServerPort
    private int port;

    @Autowired
    private AuthTokenProvider authTokenProvider;

    private String accessToken;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        long memberId = 1L;
        accessToken = TestSupport.getAccessTokenByMemberId(memberId, authTokenProvider);
    }

    @DisplayName("멤버의 응원팀을 조회한다")
    @Test
    void findFavorites() {
        // given
        String expected = "기아";

        // when
        MemberFavoriteResponse actual = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .when().get("/api/members/favorites")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(MemberFavoriteResponse.class);

        // then
        assertThat(actual.favorite()).isEqualTo(expected);
    }

    @DisplayName("회원 탈퇴한다")
    @Test
    void removeMember() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .when().delete("/api/members/me")
                .then().log().all()
                .statusCode(204);
    }
}
