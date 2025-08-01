package com.yagubogu.talk;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;

import com.yagubogu.talk.dto.TalkRequest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = {
        "spring.sql.init.data-locations=classpath:talk-test-data.sql"
})
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class TalkIntegrationTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @DisplayName("톡의 첫 페이지를 조회한다")
    @Test
    void findTalks_firstPage() {
        // given
        long gameId = 1L;

        // when & then
        given()
                .queryParam("limit", 10)
                .queryParam("memberId", 1)
                .when()
                .get("/api/talks/{gameId}", gameId)
                .then()
                .statusCode(200)
                .body("content[0].id", is(52))
                .body("nextCursorId", is(43));
    }

    @DisplayName("톡의 중간 페이지를 조회한다")
    @Test
    void findTalks_middlePage() {
        // given
        long gameId = 1L;

        // when & then
        given()
                .queryParam("before", 25)
                .queryParam("limit", 10)
                .queryParam("memberId", 1)
                .when()
                .get("/api/talks/{gameId}", gameId)
                .then()
                .statusCode(200)
                .body("content[0].id", is(24))
                .body("nextCursorId", is(15));
    }

    @DisplayName("톡의 마지막 페이지를 조회한다")
    @Test
    void findTalks_lastPage() {
        // given
        long gameId = 1L;

        // when & then
        given()
                .queryParam("before", 6)
                .queryParam("limit", 10)
                .queryParam("memberId", 1)
                .when()
                .get("/api/talks/{gameId}", gameId)
                .then()
                .statusCode(200)
                .body("content[0].id", is(5))
                .body("nextCursorId", is(nullValue()));
    }

    @DisplayName("새 톡을 가져온다")
    @Test
    void pollTalks_existing() {
        // given
        long gameId = 1L;

        // when & then
        given()
                .queryParam("after", 47)
                .queryParam("limit", 10)
                .when()
                .get("/api/talks/{gameId}/polling", gameId)
                .then()
                .statusCode(200)
                .body("content.size()", is(5))
                .body("content[-1].id", is(52))
                .body("nextCursorId", is(52));
    }

    @DisplayName("새 톡이 없다면 가져오지 않는다")
    @Test
    void pollTalks_noExisting() {
        // given
        long gameId = 1L;

        // when & then
        given()
                .queryParam("after", 52)
                .queryParam("limit", 10)
                .when()
                .get("/api/talks/{gameId}/polling", gameId)
                .then()
                .statusCode(200)
                .body("content.size()", is(0))
                .body("nextCursorId", is(52));
    }

    @DisplayName("정상적으로 톡을 저장하고 응답을 반환한다")
    @Test
    void createTalk() {
        // given
        long gameId = 1L;
        String content = "오늘 야구보구 인증하구";

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(new TalkRequest(content))
                .queryParam("memberId", 1)
                .when().post("/api/talks/{gameId}", gameId)
                .then().log().all()
                .statusCode(201)
                .body("memberId", is(1))
                .body("nickname", is("포라"))
                .body("favorite", is("롯데"))
                .body("content", is(content));
    }

    @DisplayName("톡을 삭제한다")
    @Test
    void removeTalk() {
        // given
        long gameId = 1L;
        long talkId = 9L;

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .queryParam("memberId", 1)
                .when().delete("/api/talks/{gameId}/{talkId}", gameId, talkId)
                .then().log().all()
                .statusCode(204);
    }

    @DisplayName("톡을 신고한다")
    @Test
    void reportTalk() {
        // given
        long talkId = 9L;

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .queryParam("reporterId", 2)
                .when().post("/api/talks/{talkId}/reports", talkId)
                .then().log().all()
                .statusCode(201);
    }
}
