package com.yagubogu.restaurant;

import static org.assertj.core.api.Assertions.assertThat;

import com.yagubogu.auth.config.AuthTestConfig;
import com.yagubogu.global.config.JpaAuditingConfig;
import com.yagubogu.restaurant.domain.Restaurant;
import com.yagubogu.restaurant.dto.v1.RestaurantsResponse;
import com.yagubogu.restaurant.repository.RestaurantRepository;
import com.yagubogu.support.base.E2eTestBase;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;

@Import({AuthTestConfig.class, JpaAuditingConfig.class})
class RestaurantE2eTest extends E2eTestBase {

    // 잠실야구장 (Flyway R__seed_lookup.sql로 시드된 구장 ID)
    private static final Long JAMSIL_STADIUM_ID = 2L;

    @LocalServerPort
    private int port;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @DisplayName("구장 ID로 주변 맛집 목록을 조회하면 저장된 맛집 정보가 반환된다")
    @Test
    void getRestaurantsByStadium_맛집_목록_반환() {
        // given
        restaurantRepository.save(
                Restaurant.of("1001", JAMSIL_STADIUM_ID, "잠실 원조 순대국밥",
                        "서울 송파구 올림픽로 25", 127.0715, 37.5122, 320, "02-123-4567", null));
        restaurantRepository.save(
                Restaurant.of("1002", JAMSIL_STADIUM_ID, "잠실 청국장",
                        "서울 송파구 잠실동 10", 127.0720, 37.5130, 150, null, "https://img.com/img.jpg"));

        // when & then
        RestaurantsResponse response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .queryParam("stadiumId", JAMSIL_STADIUM_ID)
                .when().get("/api/v1/restaurants")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(RestaurantsResponse.class);

        assertThat(response.stadiumId()).isEqualTo(JAMSIL_STADIUM_ID);
        assertThat(response.restaurants()).hasSize(2);
        assertThat(response.restaurants()).extracting(r -> r.title())
                .containsExactlyInAnyOrder("잠실 원조 순대국밥", "잠실 청국장");
    }

    @DisplayName("맛집이 없는 구장을 조회하면 빈 목록이 반환된다")
    @Test
    void getRestaurantsByStadium_맛집_없으면_빈_리스트() {
        // when & then
        RestaurantsResponse response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .queryParam("stadiumId", JAMSIL_STADIUM_ID)
                .when().get("/api/v1/restaurants")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(RestaurantsResponse.class);

        assertThat(response.stadiumId()).isEqualTo(JAMSIL_STADIUM_ID);
        assertThat(response.restaurants()).isEmpty();
    }

    @DisplayName("stadiumId 없이 요청하면 400 Bad Request를 반환한다")
    @Test
    void getRestaurantsByStadium_stadiumId_누락_400() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().get("/api/v1/restaurants")
                .then().log().all()
                .statusCode(400);
    }

    @DisplayName("맛집의 좌표(mapX, mapY)와 거리(distance) 정보가 응답에 포함된다")
    @Test
    void getRestaurantsByStadium_좌표_거리_포함() {
        // given
        restaurantRepository.save(
                Restaurant.of("2001", JAMSIL_STADIUM_ID, "좌표 맛집",
                        "서울 송파구", 127.0715, 37.5122, 300, "02-999-8888", null));

        // when & then
        RestaurantsResponse response = RestAssured.given()
                .queryParam("stadiumId", JAMSIL_STADIUM_ID)
                .when().get("/api/v1/restaurants")
                .then()
                .statusCode(200)
                .extract()
                .as(RestaurantsResponse.class);

        var restaurant = response.restaurants().get(0);
        assertThat(restaurant.mapX()).isEqualTo(127.0715);
        assertThat(restaurant.mapY()).isEqualTo(37.5122);
        assertThat(restaurant.distance()).isEqualTo(300);
        assertThat(restaurant.tel()).isEqualTo("02-999-8888");
    }
}
