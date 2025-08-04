package com.yagubogu.stadium;

import com.yagubogu.stadium.dto.StadiumResponse;
import com.yagubogu.stadium.dto.StadiumsResponse;
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

import static org.assertj.core.api.Assertions.assertThat;

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
        // given
        List<StadiumResponse> expected = getStadiums();

        // when
        StadiumsResponse actual = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .queryParams("memberId", 1L, "year", 2025)
                .when().get("/api/stadiums")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(StadiumsResponse.class);

        // then
        assertThat(actual.stadiums()).isEqualTo(expected);
    }


    private List<StadiumResponse> getStadiums() {
        return List.of(
                new StadiumResponse(1L, "잠실 야구장", "잠실구장", "잠실", 37.512192, 127.072055),
                new StadiumResponse(2L, "고척 스카이돔", "고척돔", "고척", 37.498191, 126.867073),
                new StadiumResponse(3L, "인천 SSG 랜더스필드", "랜더스필드", "인천", 37.437196, 126.693294),
                new StadiumResponse(4L, "대전 한화생명 볼파크", "볼파크", "대전", 36.316589, 127.431211),
                new StadiumResponse(5L, "광주 KIA 챔피언스필드", "챔피언스필드", "광주", 35.168282, 126.889138),
                new StadiumResponse(6L, "대구 삼성라이온즈파크", "라이온즈파크", "대구", 35.841318, 128.681559),
                new StadiumResponse(7L, "창원 NC파크", "엔씨파크", "창원", 35.222754, 128.582251),
                new StadiumResponse(8L, "수원 KT위즈파크", "위즈파크", "수원", 37.299977, 127.009690),
                new StadiumResponse(9L, "부산 사직야구장", "사직구장", "부산", 35.194146, 129.061497)
        );
    }
}
