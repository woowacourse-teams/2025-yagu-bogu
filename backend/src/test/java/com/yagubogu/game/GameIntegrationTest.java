package com.yagubogu.game;

import com.yagubogu.fixture.TestFixture;
import com.yagubogu.game.dto.GameResponse;
import com.yagubogu.game.dto.GameWithCheckIn;
import com.yagubogu.game.dto.StadiumByGame;
import com.yagubogu.game.dto.TeamByGame;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@TestPropertySource(properties = {
        "spring.sql.init.data-locations=classpath:test-data.sql"
})
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class GameIntegrationTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @DisplayName("경기하고 있는 모든 구장, 팀을 조회한다")
    @Test
    void findGamesByDate() {
        // given
        long memberId = 1L;
        LocalDate date = TestFixture.getToday();
        List<GameWithCheckIn> expected = List.of(
                new GameWithCheckIn(
                        3L,
                        true,
                        new StadiumByGame(1L, "잠실 야구장"),
                        new TeamByGame(1L, "기아", "HT"),
                        new TeamByGame(2L, "롯데", "LT")),
                new GameWithCheckIn(
                        2L,
                        false,
                        new StadiumByGame(2L, "고척 스카이돔"),
                        new TeamByGame(3L, "삼성", "SS"),
                        new TeamByGame(4L, "두산", "OB")),
                new GameWithCheckIn(
                        4L,
                        false,
                        new StadiumByGame(3L, "인천 SSG 랜더스필드"),
                        new TeamByGame(5L, "LG", "LG"),
                        new TeamByGame(6L, "KT", "KT"))
        );

        // when
        GameResponse actual = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .queryParam("date", date.toString())
                .queryParam("memberId", memberId)
                .when().get("/api/games")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(GameResponse.class);

        // then
        assertThat(actual.games()).containsExactlyInAnyOrderElementsOf(expected);
    }

    @DisplayName("예외: 미래 날짜를 조회하려고 하면 예외가 발생한다")
    @Test
    void findGamesByDate_WhenDateIsInFuture() {
        // given
        long memberId = 1L;
        LocalDate invalidDate = LocalDate.of(3000, 12, 12);

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .queryParam("date", invalidDate.toString())
                .queryParam("memberId", memberId)
                .when().get("/api/games")
                .then().log().all()
                .statusCode(422);
    }

    @DisplayName("예외: 해당하는 회원을 찾지 못하면 404 상태 코드를 반환한다")
    @Test
    void findGamesByDate_notFoundMember() {
        // given
        long invalidMemberId = 999L;
        LocalDate date = TestFixture.getToday();

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .queryParam("date", date.toString())
                .queryParam("memberId", invalidMemberId)
                .when().get("/api/games")
                .then().log().all()
                .statusCode(404);
    }
}
