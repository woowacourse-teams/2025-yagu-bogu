package com.yagubogu.game;

import com.yagubogu.fixture.TestFixture;
import com.yagubogu.game.dto.GamesResponse;
import com.yagubogu.game.dto.GamesResponse.GameResponse;
import com.yagubogu.game.dto.GamesResponse.StadiumInfoResponse;
import com.yagubogu.game.dto.GamesResponse.TeamInfoResponse;
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
        LocalDate date = TestFixture.getToday();
        List<GameResponse> expected = List.of(
                new GameResponse(
                        new StadiumInfoResponse(1L, "잠실 야구장"),
                        new TeamInfoResponse(1L, "기아 타이거즈", "HT"),
                        new TeamInfoResponse(2L, "롯데 자이언츠", "LT")
                )
        );

        // when
        GamesResponse actual = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .queryParam("date", date.toString())
                .when().get("/api/games")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(GamesResponse.class);

        // then
        assertThat(actual.games()).containsExactlyElementsOf(expected);
    }

    @DisplayName("예외: 미래 날짜를 조회하려고 하면 예외가 발생한다")
    @Test
    void findGamesByDate_WhenDateIsInFuture() {
        // given
        LocalDate invalidDate = LocalDate.of(3000, 12, 12);

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .queryParam("date", invalidDate.toString())
                .when().get("/api/games")
                .then().log().all()
                .statusCode(422);
    }
}
