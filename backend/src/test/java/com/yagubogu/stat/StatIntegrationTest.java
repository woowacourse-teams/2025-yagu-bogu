package com.yagubogu.stat;

import com.yagubogu.stat.dto.AverageStatisticResponse;
import com.yagubogu.stat.dto.LuckyStadiumResponse;
import com.yagubogu.stat.dto.StatCountsResponse;
import com.yagubogu.stat.dto.WinRateResponse;
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

import static org.assertj.core.api.Assertions.assertThat;

@TestPropertySource(properties = {
        "spring.sql.init.data-locations=classpath:test-data.sql"
})
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class StatIntegrationTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @DisplayName("승패무 횟수와 총 직관 횟수를 조회한다")
    @Test
    void findStatCounts() {
        // when
        StatCountsResponse expected = new StatCountsResponse(5, 1, 0, 6);

        // given
        StatCountsResponse actual = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .queryParams("memberId", 1L, "year", 2025)
                .when().get("/api/stats/counts")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(StatCountsResponse.class);

        // then
        assertThat(actual).isEqualTo(expected);
    }

    @DisplayName("직관 승률을 조회한다")
    @Test
    void findWinRate() {
        // given
        WinRateResponse expected = new WinRateResponse(83.3);

        // when
        WinRateResponse actual = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .queryParams("memberId", 1L, "year", 2025)
                .when().get("/api/stats/win-rate")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(WinRateResponse.class);

        // then
        assertThat(actual).isEqualTo(expected);
    }

    @DisplayName("예외: 관리자일 경우 직관 승률을 조회하면 예외가 발생한다")
    @Test
    void findWinRate_whenAdmin() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .queryParams("memberId", 4L, "year", 2025)
                .when().get("/api/stats/win-rate")
                .then().log().all()
                .statusCode(403);
    }

    @DisplayName("행운의 구장을 조회한다")
    @Test
    void findLuckyStadium() {
        // given
        LuckyStadiumResponse expected = new LuckyStadiumResponse("챔피언스필드");

        // when
        LuckyStadiumResponse actual = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .queryParams("memberId", 1L, "year", 2025)
                .when().get("/api/stats/lucky-stadiums")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(LuckyStadiumResponse.class);

        // then
        assertThat(actual).isEqualTo(expected);
    }

    @DisplayName("평균 득, 실, 실책, 안타, 피안타 조회한다")
    @Test
    void findAverageStatistic() {
        // given
        AverageStatisticResponse expected = new AverageStatisticResponse(
                7.9,
                6.0,
                0.3,
                10.6,
                8.3
        );

        // when
        AverageStatisticResponse actual = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .queryParams("memberId", 1L)
                .when().get("/api/stats/teams/fan")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(AverageStatisticResponse.class);

        // then
        assertThat(actual).isEqualTo(expected);
    }

    @DisplayName("예외: 멤버ID에 해당하는 회원을 찾지 못하면 예외가 발생한다")
    @Test
    void findAverageStatistic_notFoundMember() {
        // given
        long invalidMemberId = 999L;

        // when
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .queryParams("memberId", invalidMemberId)
                .when().get("/api/stats/teams/fan")
                .then().log().all()
                .statusCode(404);
    }
}
