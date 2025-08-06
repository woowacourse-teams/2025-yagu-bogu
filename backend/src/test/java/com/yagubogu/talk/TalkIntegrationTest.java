package com.yagubogu.talk;

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
        RestAssured.given()
                .queryParam("limit", 10)
                .queryParam("memberId", 1)
                .pathParam("gameId", gameId)
                .when()
                .get("/api/talks/{gameId}")
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
        RestAssured.given()
                .queryParam("before", 25)
                .queryParam("limit", 10)
                .queryParam("memberId", 1)
                .pathParam("gameId", gameId)
                .when()
                .get("/api/talks/{gameId}")
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
        RestAssured.given()
                .queryParam("before", 6)
                .queryParam("limit", 10)
                .queryParam("memberId", 1)
                .pathParam("gameId", gameId)
                .when()
                .get("/api/talks/{gameId}")
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
        RestAssured.given()
                .queryParam("after", 47)
                .queryParam("limit", 10)
                .pathParam("gameId", gameId)
                .when()
                .get("/api/talks/{gameId}/polling")
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
        RestAssured.given()
                .queryParam("after", 52)
                .queryParam("limit", 10)
                .pathParam("gameId", gameId)
                .when()
                .get("/api/talks/{gameId}/polling")
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
                .pathParam("gameId", gameId)
                .when().post("/api/talks/{gameId}")
                .then().log().all()
                .statusCode(201)
                .body("memberId", is(1))
                .body("nickname", is("포라"))
                .body("favorite", is("롯데"))
                .body("content", is(content));
    }

    @DisplayName("예외: 신고를 총 10명 이상에게 받은 사용자는 톡을 생성할 수 없다")
    @Test
    void createTalk_blockedFromStadium() {
        // given
        long gameId = 1L;
        long blockedMemberId = 2L;
        String content = "오늘 야구보구 인증하구";

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(new TalkRequest(content))
                .queryParam("memberId", blockedMemberId)
                .pathParam("gameId", gameId)
                .when()
                .post("/api/talks/{gameId}")
                .then().log().all()
                .statusCode(403);
    }

    @DisplayName("예외: 존재하지 않는 gameId로 톡을 생성하면 에러가 발생한다")
    @Test
    void createTalk_withInvalidGameId() {
        // given
        long gameId = 999L;
        String content = "오늘 야구보구 인증하구";

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(new TalkRequest(content))
                .queryParam("memberId", 1)
                .pathParam("gameId", gameId)
                .when().post("/api/talks/{gameId}")
                .then().log().all()
                .statusCode(404);
    }

    @DisplayName("예외: 존재하지 않는 memberId로 톡을 생성하면 에러가 발생한다")
    @Test
    void createTalk_withInvalidMemberId() {
        // given
        long gameId = 1L;
        String content = "오늘 야구보구 인증하구";

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(new TalkRequest(content))
                .queryParam("memberId", 999)
                .pathParam("gameId", gameId)
                .when().post("/api/talks/{gameId}")
                .then().log().all()
                .statusCode(404);
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
                .pathParam("gameId", gameId)
                .pathParam("talkId", talkId)
                .when().delete("/api/talks/{gameId}/{talkId}")
                .then().log().all()
                .statusCode(204);
    }

    @DisplayName("예외: 존재하지 않는 talkId로 톡을 삭제하면 예외가 발생한다")
    @Test
    void removeTalk_withInvalidTalkId() {
        // given
        long gameId = 1L;
        long talkId = 999L;

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .queryParam("memberId", 1)
                .pathParam("gameId", gameId)
                .pathParam("talkId", talkId)
                .when().delete("/api/talks/{gameId}/{talkId}")
                .then().log().all()
                .statusCode(404);
    }

    @DisplayName("예외: talk의 gameId와 요청 gameId가 일치하지 않으면 예외가 발생한다")
    @Test
    void removeTalk_withMismatchedGameId() {
        // given
        long gameId = 2L;
        long talkId = 31L;

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .queryParam("memberId", 1)
                .pathParam("gameId", gameId)
                .pathParam("talkId", talkId)
                .when().delete("/api/talks/{gameId}/{talkId}")
                .then().log().all()
                .statusCode(400);
    }

    @DisplayName("예외: talk의 memberId와 요청 memberId가 일치하지 않으면 예외가 발생한다")
    @Test
    void removeTalk_withMismatchedMemberId() {
        // given
        long gameId = 1L;
        long talkId = 31L;

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .queryParam("memberId", 2)
                .pathParam("gameId", gameId)
                .pathParam("talkId", talkId)
                .when().delete("/api/talks/{gameId}/{talkId}")
                .then().log().all()
                .statusCode(403);
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
                .pathParam("talkId", talkId)
                .when().post("/api/talks/{talkId}/reports")
                .then().log().all()
                .statusCode(201);
    }
}
