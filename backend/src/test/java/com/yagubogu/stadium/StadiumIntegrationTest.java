package com.yagubogu.stadium;

import com.yagubogu.stadium.dto.StadiumResponse;
import com.yagubogu.stadium.dto.StadiumsResponse;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
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

    @DisplayName("전체 구장 목록을 조회한다")
    @Test
    void findAllStadiums() {
        StadiumsResponse actual = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .queryParams("memberId", 1L, "year", 2025)
                .when().get("/api/stadiums")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(StadiumsResponse.class);

        List<StadiumResponse> expected = List.of(
                new StadiumResponse(1L, "잠실 야구장"),
                new StadiumResponse(2L, "고척 스카이돔"),
                new StadiumResponse(3L, "인천 SSG 랜더스필드"),
                new StadiumResponse(4L, "대전 한화생명 볼파크"),
                new StadiumResponse(5L, "광주 KIA 챔피언스필드"),
                new StadiumResponse(6L, "대구 삼성라이온즈파크"),
                new StadiumResponse(7L, "창원 NC파크"),
                new StadiumResponse(8L, "수원 KT위즈파크"),
                new StadiumResponse(9L, "부산 사직야구장")
        );

        Assertions.assertThat(actual.stadiums()).isEqualTo(expected);
    }

}
