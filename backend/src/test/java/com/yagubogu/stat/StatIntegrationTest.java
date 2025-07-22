package com.yagubogu.stat;

import com.yagubogu.stat.dto.LuckyStadiumResponse;
import com.yagubogu.stat.dto.StatCountsResponse;
import com.yagubogu.stat.dto.WinRateResponse;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.assertj.core.api.Assertions;
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
        StatCountsResponse actual = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .queryParams("memberId", 1L, "year", 2025)
                .when().get("/api/stats/counts")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(StatCountsResponse.class);

        StatCountsResponse expected = new StatCountsResponse(5, 1, 0, 6);

        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @DisplayName("직관 승률을 조회한다")
    @Test
    void findWinRate() {
        WinRateResponse actual = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .queryParams("memberId", 1L, "year", 2025)
                .when().get("/api/stats/win-rate")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(WinRateResponse.class);

        WinRateResponse expected = new WinRateResponse(83.3);

        Assertions.assertThat(actual).isEqualTo(expected);
    }

    @DisplayName("행운의 구장을 조회한다")
    @Test
    void findLuckyStadium() {
        LuckyStadiumResponse actual = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .queryParams("memberId", 1L, "year", 2025)
                .when().get("/api/stats/lucky-stadiums")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(LuckyStadiumResponse.class);

        LuckyStadiumResponse expected = new LuckyStadiumResponse("챔피언스필드");

        Assertions.assertThat(actual).isEqualTo(expected);
    }
}



