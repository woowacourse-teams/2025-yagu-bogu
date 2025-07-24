package com.yagubogu.checkin;

import static org.assertj.core.api.Assertions.assertThat;

import com.yagubogu.checkin.dto.CheckInCountsResponse;
import com.yagubogu.checkin.dto.CreateCheckInRequest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
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
public class CheckInIntegrationTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @DisplayName("인증을 저장한다")
    @Test
    void createCheckIn() {
        // given
        long memberId = 5L;
        long stadiumId = 1L;
        LocalDate date = LocalDate.of(2025, 7, 19);

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(new CreateCheckInRequest(memberId, stadiumId, date))
                .when().post("/api/check-ins")
                .then().log().all()
                .statusCode(201);
    }

    @DisplayName("회원의 총 인증 횟수를 조회한다")
    @Test
    void findCheckInCounts() {
        CheckInCountsResponse actual = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .queryParams("memberId", 1L, "year", 2025)
                .when().get("/api/check-ins/counts")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(CheckInCountsResponse.class);

        assertThat(actual.checkInCounts()).isEqualTo(6);
    }

    @DisplayName("예외: 인증할 때 구장이 없으면 예외가 발생한다")
    @Test
    void createCheckIn_notFoundStadium() {
        // given
        long memberId = 1L;
        long invalidStadiumId = 999L;
        LocalDate date = LocalDate.of(2025, 7, 21);

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(new CreateCheckInRequest(memberId, invalidStadiumId, date))
                .when().post("/api/check-ins")
                .then().log().all()
                .statusCode(404);
    }

    @DisplayName("예외: 인증할 때 게임이 없으면 예외가 발생한다")
    @Test
    void createCheckIn_notFoundGame() {
        // given
        long memberId = 1L;
        long stadiumId = 1L;
        LocalDate invalidDate = LocalDate.of(1000, 7, 21);

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(new CreateCheckInRequest(memberId, stadiumId, invalidDate))
                .when().post("/api/check-ins")
                .then().log().all()
                .statusCode(404);
    }

    @DisplayName("예외: 인증할 때 회원이 없으면 예외가 발생한다")
    @Test
    void createCheckIn_notFoundMember() {
        // given
        long invalidMemberId = 999L;
        long stadiumId = 1L;
        LocalDate date = LocalDate.of(2025, 7, 21);

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(new CreateCheckInRequest(invalidMemberId, stadiumId, date))
                .when().post("/api/check-ins")
                .then().log().all()
                .statusCode(404);
    }
}
