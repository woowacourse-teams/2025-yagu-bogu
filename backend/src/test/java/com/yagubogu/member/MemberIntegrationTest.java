package com.yagubogu.member;

import static org.assertj.core.api.Assertions.assertThat;

import com.yagubogu.auth.config.AuthTestConfig;
import com.yagubogu.fixture.TestSupport;
import com.yagubogu.member.dto.MemberFavoriteResponse;
import com.yagubogu.member.dto.MemberNicknameRequest;
import com.yagubogu.member.dto.MemberNicknameResponse;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @DisplayName("멤버의 응원팀을 조회한다")
    @Test
    void findFavorites() {
        // given
        String expected = "기아";

        // when
        MemberFavoriteResponse actual = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .pathParam("memberId", 1L)
                .when().get("/api/members/{memberId}/favorites")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(MemberFavoriteResponse.class);

        // then
        assertThat(actual.favorite()).isEqualTo(expected);
    }

    @DisplayName("멤버의 닉네임을 조회한다")
    @Test
    void findNickName() {
        // given
        String expected = "포라";

        // when
        MemberNicknameResponse actual = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .pathParam("memberId", 2L)
                .when().get("/api/members/me/{memberId}/nickname")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(MemberNicknameResponse.class);

        // then
        assertThat(actual.nickname()).isEqualTo(expected);
    }

    @DisplayName("멤버의 닉네임을 수정한다")
    @Test
    void patchNickname() {
        // given
        String expected = "바꾼닉";

        // when
        MemberNicknameResponse actual = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .pathParam("memberId", 2L)
                .body(new MemberNicknameRequest("바꾼닉"))
                .when().patch("/api/members/me/{memberId}/nickname")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(MemberNicknameResponse.class);

        // then
        assertThat(actual.nickname()).isEqualTo(expected);
    }

    @DisplayName("회원 탈퇴한다")
    @Test
    void removeMember() {
        // given
        String accessToken = TestSupport.getAccessToken("id_token");

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .when().delete("/api/members/me")
                .then().log().all()
                .statusCode(204);
    }
}
