package com.yagubogu.checkin;

import com.yagubogu.auth.config.AuthTestConfig;
import com.yagubogu.auth.support.AuthTokenProvider;
import com.yagubogu.checkin.domain.CheckInResultFilter;
import com.yagubogu.checkin.dto.CheckInCountsResponse;
import com.yagubogu.checkin.dto.CheckInStatusResponse;
import com.yagubogu.checkin.dto.CreateCheckInRequest;
import com.yagubogu.support.TestFixture;
import com.yagubogu.support.TestSupport;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@Import(AuthTestConfig.class)
@TestPropertySource(properties = {
        "spring.sql.init.data-locations=classpath:test-data.sql"
})
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class CheckInIntegrationTest {

    private static final String ID_TOKEN = "ID_TOKEN";

    @LocalServerPort
    private int port;

    @Autowired
    private AuthTokenProvider authTokenProvider;

    private String accessToken;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        accessToken = TestSupport.getAccessToken(ID_TOKEN);
    }

    @DisplayName("인증을 저장한다")
    @Test
    void createCheckIn() {
        // given
        accessToken = TestSupport.getAccessTokenByMemberId(5L, authTokenProvider);
        long stadiumId = 1L;
        LocalDate date = LocalDate.of(2025, 7, 19);

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .body(new CreateCheckInRequest(stadiumId, date))
                .when().post("/api/check-ins")
                .then().log().all()
                .statusCode(201);
    }

    @DisplayName("회원의 총 인증 횟수를 조회한다")
    @Test
    void findCheckInCounts() {
        // given
        accessToken = TestSupport.getAccessTokenByMemberId(1L, authTokenProvider);

        // when
        CheckInCountsResponse actual = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .queryParams("year", 2025)
                .when().get("/api/check-ins/counts")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(CheckInCountsResponse.class);

        // then
        assertThat(actual.checkInCounts()).isEqualTo(7);
    }

    @DisplayName("예외: 인증할 때 구장이 없으면 예외가 발생한다")
    @Test
    void createCheckIn_notFoundStadium() {
        // given
        long memberId = 1L;
        long invalidStadiumId = 999L;
        LocalDate date = LocalDate.of(2025, 7, 21);
        accessToken = TestSupport.getAccessTokenByMemberId(memberId, authTokenProvider);

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .body(new CreateCheckInRequest(invalidStadiumId, date))
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
        accessToken = TestSupport.getAccessTokenByMemberId(memberId, authTokenProvider);

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .body(new CreateCheckInRequest(stadiumId, invalidDate))
                .when().post("/api/check-ins")
                .then().log().all()
                .statusCode(404);
    }

    @DisplayName("직관 내역을 조회한다")
    @Test
    void findCheckInHistory() {
        // given
        long memberId = 1L;
        accessToken = TestSupport.getAccessTokenByMemberId(memberId, authTokenProvider);

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .queryParam("year", 2025)
                .queryParam("result", CheckInResultFilter.ALL)
                .when().get("/api/check-ins/members")
                .then().log().all()
                .statusCode(200);
    }

    @DisplayName("승리 요정 랭킹을 조회한다")
    @Test
    void findVictoryFairyRankings() {
        // given
        long memberId = 1L;
        accessToken = TestSupport.getAccessTokenByMemberId(memberId, authTokenProvider);

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .when().get("/api/check-ins/victory-fairy/rankings")
                .then().log().all()
                .statusCode(200);
    }

    @DisplayName("인증 여부를 조회한다")
    @Test
    void findCheckInStatus() {
        // given
        long memberId = 1L;
        accessToken = TestSupport.getAccessTokenByMemberId(memberId, authTokenProvider);

        // when
        CheckInStatusResponse actual = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .queryParam("date", "2025-07-21")
                .when().get("/api/check-ins/status")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(CheckInStatusResponse.class);

        // then
        assertThat(actual.isCheckIn()).isTrue();
    }

    @DisplayName("이긴 직관 내역을 조회한다")
    @Test
    void findCheckInWinHistory() {
        // given
        long memberId = 1L;
        accessToken = TestSupport.getAccessTokenByMemberId(memberId, authTokenProvider);

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .queryParam("year", 2025)
                .queryParam("result", CheckInResultFilter.WIN)
                .when().get("/api/check-ins/members")
                .then().log().all()
                .statusCode(200);
    }

    @DisplayName("오늘 경기하는 모든 구장 별 팬 점유율을 조회한다")
    @Test
    void findFanRatesByStadiums() {
        // given
        long memberId = 1L;
        accessToken = TestSupport.getAccessTokenByMemberId(memberId, authTokenProvider);

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .queryParam("date", TestFixture.getToday().toString())
                .when().get("/api/check-ins/stadiums/fan-rates")
                .then().log().all()
                .statusCode(200);
    }
}
