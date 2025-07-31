package com.yagubogu.talk;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties =
        "spring.sql.init.data-locations=classpath:talk-data.sql"
)
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
        Long gameId = 1L;

        // when & then
        given()
                .queryParam("limit", 10)
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
        Long gameId = 1L;

        // when & then
        given()
                .queryParam("before", 25)
                .queryParam("limit", 10)
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
        Long gameId = 1L;

        // when & then
        given()
                .queryParam("before", 6)
                .queryParam("limit", 10)
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
        Long gameId = 1L;

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
        Long gameId = 1L;

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
}
