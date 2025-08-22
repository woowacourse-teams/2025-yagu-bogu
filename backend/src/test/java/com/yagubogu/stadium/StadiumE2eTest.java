package com.yagubogu.stadium;

import static org.assertj.core.api.Assertions.assertThat;

import com.yagubogu.stadium.dto.StadiumResponse;
import com.yagubogu.stadium.dto.StadiumsResponse;
import com.yagubogu.support.E2eTestBase;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;

public class StadiumE2eTest extends E2eTestBase {

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
                new StadiumResponse(1L, "광주 기아 챔피언스필드", "챔피언스필드", "광주", 35.168139, 126.889111),
                new StadiumResponse(2L, "잠실 야구장", "잠실구장", "잠실", 37.512150, 127.071976),
                new StadiumResponse(3L, "고척 스카이돔", "고척돔", "고척", 37.498222, 126.867250),
                new StadiumResponse(4L, "수원 KT 위즈파크", "위즈파크", "수원", 37.299759, 127.009781),
                new StadiumResponse(5L, "대구 삼성 라이온즈파크", "라이온즈파크", "대구", 35.841111, 128.681667),
                new StadiumResponse(6L, "사직야구장", "사직구장", "부산", 35.194077, 129.061584),
                new StadiumResponse(7L, "인천 SSG 랜더스필드", "랜더스필드", "인천", 37.436778, 126.693306),
                new StadiumResponse(8L, "창원 NC 파크", "엔씨파크", "창원", 35.222754, 128.582251),
                new StadiumResponse(9L, "대전 한화생명 볼파크", "볼파크", "대전", 36.316589, 127.431211)
        );
    }
}
