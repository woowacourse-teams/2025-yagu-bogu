package com.yagubogu.stadium;

import static org.assertj.core.api.Assertions.assertThat;

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

@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class StadiumE2eTest {

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
                new StadiumResponse(1L, "챔피언스필드", "챔피언스필드", "광주", 35.1683, 126.8889),
                new StadiumResponse(2L, "잠실야구장", "잠실구장", "잠실", 37.5121, 127.0710),
                new StadiumResponse(3L, "고척스카이돔", "고척돔", "고척", 37.4982, 126.8676),
                new StadiumResponse(4L, "수원KT위즈파크", "위즈파크", "수원", 37.2996, 126.9707),
                new StadiumResponse(5L, "대구삼성라이온즈파크", "라이온즈파크", "대구", 35.8419, 128.6815),
                new StadiumResponse(6L, "사직야구장", "사직구장", "부산", 35.1943, 129.0615),
                new StadiumResponse(7L, "문학야구장", "랜더스필드", "인천", 37.4361, 126.6892),
                new StadiumResponse(8L, "마산야구장", "엔씨파크", "마산", 35.2281, 128.6819),
                new StadiumResponse(9L, "이글스파크", "볼파크", "대전", 36.3173, 127.4280)
        );
    }
}
