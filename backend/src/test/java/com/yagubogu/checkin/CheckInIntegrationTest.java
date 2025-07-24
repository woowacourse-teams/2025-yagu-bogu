package com.yagubogu.checkin;

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
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(new CreateCheckInRequest(5L, 1L, LocalDate.of(2025, 7, 19)))
                .when().post("/api/check-ins")
                .then().log().all()
                .statusCode(201);
    }

    @DisplayName("예외: 인증할 때 구장이 없으면 예외가 발생한다")
    @Test
    void createCheckIn_noSuchStadium() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(new CreateCheckInRequest(1L, 999L, LocalDate.of(2025, 7, 21)))
                .when().post("/api/check-ins")
                .then().log().all()
                .statusCode(404);
    }

    @DisplayName("예외: 인증할 때 게임이 없으면 예외가 발생한다")
    @Test
    void createCheckIn_noSuchGame() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(new CreateCheckInRequest(1L, 1L, LocalDate.of(1000, 7, 21)))
                .when().post("/api/check-ins")
                .then().log().all()
                .statusCode(404);
    }

    @DisplayName("예외: 인증할 때 회원이 없으면 예외가 발생한다")
    @Test
    void createCheckIn_noSuchMember() {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(new CreateCheckInRequest(999L, 1L, LocalDate.of(2025, 7, 21)))
                .when().post("/api/check-ins")
                .then().log().all()
                .statusCode(404);
    }
}
