package com.yagubogu.stadium;

import com.yagubogu.stat.dto.OccupancyRateTotalResponse;
import com.yagubogu.stat.dto.OccupancyRateTotalResponse.OccupancyRateResponse;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.util.List;
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
public class StadiumIntegrationTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @DisplayName("오늘 구장의 팀별 점유율")
    @Test
    void findStatCounts() {
        // given
        OccupancyRateTotalResponse actual = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .pathParam("stadiumId", 1L)
                .when().queryParam("date", LocalDate.of(2025, 7, 21).toString())
                .get("/api/stadiums/{stadiumId}/occupancy-rate")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(OccupancyRateTotalResponse.class);

        OccupancyRateTotalResponse expected = new OccupancyRateTotalResponse(
                List.of(new OccupancyRateResponse(1L, "광주 KIA 챔피언스필드", 63.7))
        );

        Assertions.assertThat(actual).isEqualTo(expected);
    }
}
