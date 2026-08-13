package com.yagubogu.place;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.yagubogu.auth.config.AuthTestConfig;
import com.yagubogu.global.config.JpaAuditingConfig;
import com.yagubogu.place.domain.Place;
import com.yagubogu.place.domain.PlaceCategory;
import com.yagubogu.place.dto.v1.PlacesResponse;
import com.yagubogu.place.repository.PlaceRepository;
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
class PlaceE2eTest extends E2eTestBase {

    // 잠실야구장 (Flyway R__seed_lookup.sql로 시드된 구장 ID)
    private static final Long JAMSIL_STADIUM_ID = 2L;

    @LocalServerPort
    private int port;

    @Autowired
    private PlaceRepository placeRepository;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @DisplayName("구장 ID와 카테고리로 장소 목록을 조회하면 저장된 장소 정보가 반환된다")
    @Test
    void getPlacesByStadium_장소_목록_반환() {
        // given
        placeRepository.save(Place.of(PlaceCategory.RESTAURANT, "1001", JAMSIL_STADIUM_ID, "잠실 원조 순대국밥",
                "서울 송파구 올림픽로 25", 127.0715, 37.5122, 320, "02-123-4567", null));
        placeRepository.save(Place.of(PlaceCategory.RESTAURANT, "1002", JAMSIL_STADIUM_ID, "잠실 청국장",
                "서울 송파구 잠실동 10", 127.0720, 37.5130, 150, null, "https://img.com/img.jpg"));

        // when & then
        PlacesResponse response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .queryParam("stadiumId", JAMSIL_STADIUM_ID)
                .queryParam("category", "RESTAURANT")
                .when().get("/api/v1/places")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(PlacesResponse.class);

        assertThat(response.stadiumId()).isEqualTo(JAMSIL_STADIUM_ID);
        assertThat(response.category()).isEqualTo(PlaceCategory.RESTAURANT);
        assertThat(response.places()).hasSize(2);
        assertThat(response.places()).extracting(p -> p.title())
                .containsExactlyInAnyOrder("잠실 원조 순대국밥", "잠실 청국장");
    }

    @DisplayName("다른 카테고리로 조회하면 해당 카테고리의 장소만 반환한다")
    @Test
    void getPlacesByStadium_카테고리별로_필터링() {
        // given
        placeRepository.save(Place.of(PlaceCategory.RESTAURANT, "1001", JAMSIL_STADIUM_ID, "맛집",
                null, 127.0715, 37.5122, 320, null, null));
        placeRepository.save(Place.of(PlaceCategory.CAFE, "2001", JAMSIL_STADIUM_ID, "카페",
                null, 127.0715, 37.5122, 100, null, null));

        // when & then
        PlacesResponse response = RestAssured.given()
                .queryParam("stadiumId", JAMSIL_STADIUM_ID)
                .queryParam("category", "CAFE")
                .when().get("/api/v1/places")
                .then()
                .statusCode(200)
                .extract()
                .as(PlacesResponse.class);

        assertThat(response.places()).hasSize(1);
        assertThat(response.places().get(0).title()).isEqualTo("카페");
    }

    @DisplayName("장소가 없는 구장을 조회하면 빈 목록이 반환된다")
    @Test
    void getPlacesByStadium_장소_없으면_빈_리스트() {
        // when & then
        PlacesResponse response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .queryParam("stadiumId", JAMSIL_STADIUM_ID)
                .queryParam("category", "LODGING")
                .when().get("/api/v1/places")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(PlacesResponse.class);

        assertThat(response.stadiumId()).isEqualTo(JAMSIL_STADIUM_ID);
        assertThat(response.places()).isEmpty();
    }

    @DisplayName("stadiumId 없이 요청하면 400 Bad Request를 반환한다")
    @Test
    void getPlacesByStadium_stadiumId_누락_400() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .queryParam("category", "RESTAURANT")
                .when().get("/api/v1/places")
                .then().log().all()
                .statusCode(400);
    }

    @DisplayName("장소의 좌표(mapX, mapY)와 거리(distance) 정보가 응답에 포함된다")
    @Test
    void getPlacesByStadium_좌표_거리_포함() {
        // given
        placeRepository.save(Place.of(PlaceCategory.RESTAURANT, "2001", JAMSIL_STADIUM_ID, "좌표 맛집",
                "서울 송파구", 127.0715, 37.5122, 300, "02-999-8888", null));

        // when & then
        PlacesResponse response = RestAssured.given()
                .queryParam("stadiumId", JAMSIL_STADIUM_ID)
                .queryParam("category", "RESTAURANT")
                .when().get("/api/v1/places")
                .then()
                .statusCode(200)
                .extract()
                .as(PlacesResponse.class);

        var place = response.places().get(0);
        assertThat(place.mapX()).isEqualTo(127.0715);
        assertThat(place.mapY()).isEqualTo(37.5122);
        assertThat(place.distance()).isEqualTo(300);
        assertThat(place.tel()).isEqualTo("02-999-8888");
    }

    @DisplayName("장소 ID로 상세 정보를 조회하면 detail 정보가 포함된 응답이 반환된다")
    @Test
    void getPlaceDetail_상세_정보_반환() {
        // given
        Place saved = placeRepository.save(Place.of(PlaceCategory.RESTAURANT, "3001", JAMSIL_STADIUM_ID, "상세 맛집",
                "서울 송파구", 127.0715, 37.5122, 300, "02-999-8888", null));
        saved.updateDetailInfo("""
                {"common":{"overview":"소개글"},"intro":{"opentimefood":"11:00~22:00"}}
                """);
        placeRepository.save(saved);

        // when & then
        // detail은 카테고리(RESTAURANT)에 따라 FoodDetail 등으로 다형적으로 내려오는 sealed
        // interface라 특정 타입으로 역직렬화하지 않고 JsonPath로 필드를 검증한다.
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().get("/api/v1/places/" + saved.getId())
                .then().log().all()
                .statusCode(200)
                .body("title", equalTo("상세 맛집"))
                .body("overview", equalTo("소개글"))
                .body("detail.opentimefood", equalTo("11:00~22:00"));
    }

    @DisplayName("존재하지 않는 장소 ID로 상세 조회하면 404를 반환한다")
    @Test
    void getPlaceDetail_존재하지_않으면_404() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().get("/api/v1/places/999999")
                .then().log().all()
                .statusCode(404);
    }
}
