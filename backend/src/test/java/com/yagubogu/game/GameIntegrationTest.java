package com.yagubogu.game;

import static org.assertj.core.api.Assertions.assertThat;

import com.yagubogu.auth.config.AuthTestConfig;
import com.yagubogu.auth.support.AuthTokenProvider;
import com.yagubogu.fixture.TestFixture;
import com.yagubogu.fixture.TestSupport;
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
public class GameIntegrationTest {

    private static final String ID_TOKEN = "ID_TOKEN";

    @LocalServerPort
    private int port;

    @Autowired
    private AuthTokenProvider authTokenProvider;

    private String accessToken;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        accessToken = TestSupport.getAccessToken(ID_TOKEN);
    }

    @DisplayName("경기하고 있는 모든 구장, 팀을 조회한다")
    @Test
    void findGamesByDate() {
        // given
        accessToken = TestSupport.getAccessTokenByMemberId(1L, authTokenProvider);
        LocalDate date = TestFixture.getToday();
        List<GameWithCheckIn> expected = List.of(
                new GameWithCheckIn(
                        1L,
                        3L,
                        true,
                        new StadiumByGame(1L, "잠실 야구장"),
                        new TeamByGame(1L, "기아", "HT"),
                        new TeamByGame(2L, "롯데", "LT")),
                new GameWithCheckIn(
                        8L,
                        4L,
                        true,
                        new StadiumByGame(2L, "고척 스카이돔"),
                        new TeamByGame(3L, "삼성", "SS"),
                        new TeamByGame(4L, "두산", "OB")),
                new GameWithCheckIn(
                        9L,
                        4L,
                        false,
                        new StadiumByGame(3L, "인천 SSG 랜더스필드"),
                        new TeamByGame(5L, "LG", "LG"),
                        new TeamByGame(6L, "KT", "KT"))
        );

        // when
        GameResponse actual = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .queryParam("date", date.toString())
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
        accessToken = TestSupport.getAccessTokenByMemberId(1L, authTokenProvider);
        LocalDate invalidDate = LocalDate.of(3000, 12, 12);

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .queryParam("date", invalidDate.toString())
                .when().get("/api/games")
                .then().log().all()
                .statusCode(422);
    }
}
