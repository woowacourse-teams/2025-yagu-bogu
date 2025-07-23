package com.yagubogu.stadium;

import static com.yagubogu.fixture.TestFixture.getValidDate;
import static org.assertj.core.api.Assertions.assertThat;

import com.yagubogu.stat.dto.OccupancyRateTotalResponse;
import com.yagubogu.stat.dto.OccupancyRateTotalResponse.OccupancyRateResponse;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
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

    @DisplayName("구장별 팬 점유율을 조회한다")
    @Test
    void findStatCounts() {
        // given
        OccupancyRateTotalResponse actual = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .pathParam("stadiumId", 1L)
                .when().queryParam("date", getValidDate().toString())
                .get("/api/stadiums/{stadiumId}/occupancy-rate")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(OccupancyRateTotalResponse.class);

        // when
        OccupancyRateTotalResponse expected = new OccupancyRateTotalResponse(
                List.of(
                        new OccupancyRateResponse(1L, "기아", 66.7),
                        new OccupancyRateResponse(2L, "롯데", 33.3)
                )
        );

        // then
        assertThat(actual).isEqualTo(expected);
    }
}
