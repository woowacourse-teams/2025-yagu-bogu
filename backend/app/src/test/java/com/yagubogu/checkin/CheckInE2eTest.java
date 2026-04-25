package com.yagubogu.checkin;

import static org.assertj.core.api.Assertions.assertThat;

import com.yagubogu.auth.config.AuthTestConfig;
import com.yagubogu.checkin.domain.CheckInOrderFilter;
import com.yagubogu.checkin.domain.CheckInResultFilter;
import com.yagubogu.checkin.dto.CheckInGameParam;
import com.yagubogu.checkin.dto.StadiumCheckInCountParam;
import com.yagubogu.checkin.dto.v1.CheckInCountsResponse;
import com.yagubogu.checkin.dto.v1.CheckInHistoryResponse;
import com.yagubogu.checkin.dto.v1.CheckInImagesResponse;
import com.yagubogu.checkin.dto.v1.CheckInMemoResponse;
import com.yagubogu.checkin.dto.v1.CheckInStatusResponse;
import com.yagubogu.checkin.dto.v1.CreateCheckInRequest;
import com.yagubogu.checkin.dto.v1.StadiumCheckInCountsResponse;
import com.yagubogu.game.domain.Game;
import com.yagubogu.game.domain.GameHitterRecord;
import com.yagubogu.game.domain.GamePitcherRecord;
import com.yagubogu.game.domain.GameState;
import com.yagubogu.game.repository.GameHitterRecordRepository;
import com.yagubogu.game.repository.GamePitcherRecordRepository;
import com.yagubogu.global.config.JpaAuditingConfig;
import com.yagubogu.member.domain.Member;
import com.yagubogu.member.domain.Role;
import com.yagubogu.stadium.domain.Stadium;
import com.yagubogu.stadium.repository.StadiumRepository;
import com.yagubogu.support.TestFixture;
import com.yagubogu.support.auth.AuthFactory;
import com.yagubogu.support.base.E2eTestBase;
import com.yagubogu.support.checkin.CheckInFactory;
import com.yagubogu.support.game.GameFactory;
import com.yagubogu.support.member.MemberBuilder;
import com.yagubogu.support.member.MemberFactory;
import com.yagubogu.team.domain.Team;
import com.yagubogu.team.repository.TeamRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;

@Import({AuthTestConfig.class, JpaAuditingConfig.class})
public class CheckInE2eTest extends E2eTestBase {

    @LocalServerPort
    private int port;

    @Autowired
    private AuthFactory authFactory;

    @Autowired
    private MemberFactory memberFactory;

    @Autowired
    private GameFactory gameFactory;

    @Autowired
    private CheckInFactory checkInFactory;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private StadiumRepository stadiumRepository;

    @Autowired
    private GameHitterRecordRepository hitterRecordRepository;

    @Autowired
    private GamePitcherRecordRepository pitcherRecordRepository;

    private Team kia, kt, lg, samsung, doosan, lotte;
    private Stadium stadiumJamsil, stadiumGocheok, stadiumIncheon;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;

        kia = teamRepository.findByTeamCode("HT").orElseThrow();
        kt = teamRepository.findByTeamCode("KT").orElseThrow();
        lg = teamRepository.findByTeamCode("LG").orElseThrow();
        samsung = teamRepository.findByTeamCode("SS").orElseThrow();
        doosan = teamRepository.findByTeamCode("OB").orElseThrow();
        lotte = teamRepository.findByTeamCode("LT").orElseThrow();

        stadiumJamsil = stadiumRepository.findById(2L).orElseThrow();
        stadiumGocheok = stadiumRepository.findById(3L).orElseThrow();
        stadiumIncheon = stadiumRepository.findById(7L).orElseThrow();
    }

    @DisplayName("인증을 저장한다")
    @Test
    void createCheckIn() {
        // given
        Member fora = memberFactory.save(b -> b.team(kia));
        String accessToken = authFactory.getAccessTokenByMemberId(fora.getId(), Role.USER);

        LocalDate date = LocalDate.of(2025, 7, 25);
        Game game = gameFactory.save(builder ->
                builder.stadium(stadiumJamsil)
                        .date(date)
                        .homeTeam(kt).homeScore(10).homeScoreBoard(TestFixture.getHomeScoreBoard())
                        .awayTeam(kia).awayScore(1).awayScoreBoard(TestFixture.getAwayScoreBoard()));

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .body(new CreateCheckInRequest(game.getId()))
                .when().post("/api/v1/check-ins")
                .then().log().all()
                .statusCode(201);
    }

    @DisplayName("예외: 인증할 때 게임이 없으면 예외가 발생한다")
    @Test
    void createCheckIn_notFoundGame() {
        // given
        Member fora = memberFactory.save(b -> b.team(kia));
        String accessToken = authFactory.getAccessTokenByMemberId(fora.getId(), Role.USER);

        long invalidGameId = 999999L;

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .body(new CreateCheckInRequest(invalidGameId))
                .when().post("/api/v1/check-ins")
                .then().log().all()
                .statusCode(404);
    }

    @DisplayName("예외: 이미 회원이 경기에 인증할 경우 예외가 발생한다")
    @Test
    void createCheckIn_alreadyExists() {
        // given
        Member fora = memberFactory.save(b -> b.team(kia));
        String accessToken = authFactory.getAccessTokenByMemberId(fora.getId(), Role.USER);

        LocalDate date = LocalDate.of(2025, 7, 25);
        Game game = gameFactory.save(builder ->
                builder.stadium(stadiumJamsil)
                        .date(date)
                        .homeTeam(kt).homeScore(10).homeScoreBoard(TestFixture.getHomeScoreBoard())
                        .awayTeam(kia).awayScore(1).awayScoreBoard(TestFixture.getAwayScoreBoard()));

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .body(new CreateCheckInRequest(game.getId()))
                .when().post("/api/v1/check-ins")
                .then().log().all()
                .statusCode(201);

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .body(new CreateCheckInRequest(game.getId()))
                .when().post("/api/v1/check-ins")
                .then().log().all()
                .statusCode(409);
    }

    @DisplayName("직관 기록에 메모를 추가/수정한다")
    @Test
    void updateMemo() {
        // given
        Member fora = memberFactory.save(b -> b.team(kia));
        String accessToken = authFactory.getAccessTokenByMemberId(fora.getId(), Role.USER);

        LocalDate date = LocalDate.of(2025, 7, 28);
        Game game = gameFactory.save(builder ->
                builder.stadium(stadiumJamsil)
                        .date(date)
                        .homeTeam(kt).homeScore(10).homeScoreBoard(TestFixture.getHomeScoreBoard())
                        .awayTeam(kia).awayScore(1).awayScoreBoard(TestFixture.getAwayScoreBoard()));
        com.yagubogu.checkin.domain.CheckIn checkIn = checkInFactory.save(b -> b.game(game).team(kia).member(fora));

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .body(new com.yagubogu.checkin.dto.v1.UpdateCheckInMemoRequest("오늘 직관 너무 좋았다!"))
                .when().put("/api/v1/check-ins/" + checkIn.getId() + "/memo")
                .then().log().all()
                .statusCode(200);
    }

    @DisplayName("예외: 메모가 500자를 초과하면 예외가 발생한다")
    @Test
    void updateMemo_tooLong() {
        // given
        Member fora = memberFactory.save(b -> b.team(kia));
        String accessToken = authFactory.getAccessTokenByMemberId(fora.getId(), Role.USER);

        LocalDate date = LocalDate.of(2025, 7, 29);
        Game game = gameFactory.save(builder ->
                builder.stadium(stadiumJamsil)
                        .date(date)
                        .homeTeam(kt).homeScore(10).homeScoreBoard(TestFixture.getHomeScoreBoard())
                        .awayTeam(kia).awayScore(1).awayScoreBoard(TestFixture.getAwayScoreBoard()));
        com.yagubogu.checkin.domain.CheckIn checkIn = checkInFactory.save(b -> b.game(game).team(kia).member(fora));

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .body(new com.yagubogu.checkin.dto.v1.UpdateCheckInMemoRequest("a".repeat(501)))
                .when().put("/api/v1/check-ins/" + checkIn.getId() + "/memo")
                .then().log().all()
                .statusCode(400);
    }

    @DisplayName("직관 기록의 메모를 삭제한다")
    @Test
    void deleteMemo() {
        // given
        Member fora = memberFactory.save(b -> b.team(kia));
        String accessToken = authFactory.getAccessTokenByMemberId(fora.getId(), Role.USER);

        LocalDate date = LocalDate.of(2025, 7, 30);
        Game game = gameFactory.save(builder ->
                builder.stadium(stadiumJamsil)
                        .date(date)
                        .homeTeam(kt).homeScore(10).homeScoreBoard(TestFixture.getHomeScoreBoard())
                        .awayTeam(kia).awayScore(1).awayScoreBoard(TestFixture.getAwayScoreBoard()));
        com.yagubogu.checkin.domain.CheckIn checkIn = checkInFactory.save(b -> b.game(game).team(kia).member(fora));

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .when().delete("/api/v1/check-ins/" + checkIn.getId() + "/memo")
                .then().log().all()
                .statusCode(204);
    }

    @DisplayName("예외: 다른 회원의 직관 기록 메모는 수정할 수 없다")
    @Test
    void updateMemo_otherMember() {
        // given
        Member fora = memberFactory.save(b -> b.team(kia));
        Member other = memberFactory.save(b -> b.team(kt));
        String accessToken = authFactory.getAccessTokenByMemberId(other.getId(), Role.USER);

        LocalDate date = LocalDate.of(2025, 7, 31);
        Game game = gameFactory.save(builder ->
                builder.stadium(stadiumJamsil)
                        .date(date)
                        .homeTeam(kt).homeScore(10).homeScoreBoard(TestFixture.getHomeScoreBoard())
                        .awayTeam(kia).awayScore(1).awayScoreBoard(TestFixture.getAwayScoreBoard()));
        com.yagubogu.checkin.domain.CheckIn checkIn = checkInFactory.save(b -> b.game(game).team(kia).member(fora));

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .body(new com.yagubogu.checkin.dto.v1.UpdateCheckInMemoRequest("내 메모가 아닌데?"))
                .when().put("/api/v1/check-ins/" + checkIn.getId() + "/memo")
                .then().log().all()
                .statusCode(404);
    }

    @DisplayName("예외: 다른 회원의 직관 기록 이미지는 삭제할 수 없다")
    @Test
    void deleteImage_otherMember() {
        // given
        Member fora = memberFactory.save(b -> b.team(kia));
        Member other = memberFactory.save(b -> b.team(kt));
        String accessToken = authFactory.getAccessTokenByMemberId(other.getId(), Role.USER);

        LocalDate date = LocalDate.of(2025, 8, 1);
        Game game = gameFactory.save(builder ->
                builder.stadium(stadiumJamsil)
                        .date(date)
                        .homeTeam(kt).homeScore(10).homeScoreBoard(TestFixture.getHomeScoreBoard())
                        .awayTeam(kia).awayScore(1).awayScoreBoard(TestFixture.getAwayScoreBoard()));
        com.yagubogu.checkin.domain.CheckIn checkIn = checkInFactory.save(b -> b.game(game).team(kia).member(fora));

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .when().delete("/api/v1/check-ins/" + checkIn.getId() + "/image")
                .then().log().all()
                .statusCode(404);
    }

    @DisplayName("회원의 총 인증 횟수를 조회한다")
    @Test
    void findCheckInCounts() {
        // given
        Member fora = memberFactory.save(b -> b.team(kia));
        String accessToken = authFactory.getAccessTokenByMemberId(fora.getId(), Role.USER);

        LocalDate date = LocalDate.of(2025, 7, 25);
        Game game1 = gameFactory.save(builder ->
                builder.stadium(stadiumJamsil)
                        .date(date)
                        .homeTeam(kt).homeScore(10).homeScoreBoard(TestFixture.getHomeScoreBoard())
                        .awayTeam(kia).awayScore(1).awayScoreBoard(TestFixture.getAwayScoreBoard()));
        Game game2 = gameFactory.save(builder ->
                builder.stadium(stadiumJamsil)
                        .date(date.plusDays(1))
                        .homeTeam(kt).homeScore(10).homeScoreBoard(TestFixture.getHomeScoreBoard())
                        .awayTeam(kia).awayScore(1).awayScoreBoard(TestFixture.getAwayScoreBoard()));
        Game game3 = gameFactory.save(builder ->
                builder.stadium(stadiumJamsil)
                        .date(date.plusDays(2))
                        .homeTeam(kt).homeScore(10).homeScoreBoard(TestFixture.getHomeScoreBoard())
                        .awayTeam(kia).awayScore(1).awayScoreBoard(TestFixture.getAwayScoreBoard()));

        checkInFactory.save(b -> b.game(game1).team(kia).member(fora));
        checkInFactory.save(b -> b.game(game2).team(kia).member(fora));
        checkInFactory.save(b -> b.game(game3).team(kia).member(fora));

        // when
        CheckInCountsResponse actual = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .queryParams("year", 2025)
                .when().get("/api/v1/check-ins/counts")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(CheckInCountsResponse.class);

        // then
        assertThat(actual.checkInCounts()).isEqualTo(3);
    }

    @DisplayName("년도별 직관 내역을 최신순으로 조회한다")
    @Test
    void findCheckInHistory_findAllWithYearOrderByLatest() {
        // given
        Member fora = memberFactory.save(b -> b.team(kia));
        String accessToken = authFactory.getAccessTokenByMemberId(fora.getId(), Role.USER);

        int year = 2025;
        int month = 7;
        int day = 25;
        LocalDate date = LocalDate.of(year, month, day);
        Game game1 = gameFactory.save(builder ->
                builder.stadium(stadiumJamsil)
                        .date(date)
                        .homeTeam(kt).homeScore(10).homeScoreBoard(TestFixture.getHomeScoreBoard())
                        .awayTeam(kia).awayScore(1).awayScoreBoard(TestFixture.getAwayScoreBoard()));
        Game game2 = gameFactory.save(builder ->
                builder.stadium(stadiumJamsil)
                        .date(date.plusDays(1))
                        .homeTeam(kt).homeScore(10).homeScoreBoard(TestFixture.getHomeScoreBoard())
                        .awayTeam(kia).awayScore(1).awayScoreBoard(TestFixture.getAwayScoreBoard()));
        Game game3 = gameFactory.save(builder ->
                builder.stadium(stadiumJamsil)
                        .date(date.plusDays(2))
                        .homeTeam(kt).homeScore(10).homeScoreBoard(TestFixture.getHomeScoreBoard())
                        .awayTeam(kia).awayScore(1).awayScoreBoard(TestFixture.getAwayScoreBoard()));

        checkInFactory.save(b -> b.game(game1).team(kia).member(fora));
        checkInFactory.save(b -> b.game(game2).team(kia).member(fora));
        checkInFactory.save(b -> b.game(game3).team(kia).member(fora));

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .queryParam("year", year)
                .queryParam("result", CheckInResultFilter.ALL)
                .queryParam("order", CheckInOrderFilter.LATEST)
                .when().get("/api/v1/check-ins/members")
                .then().log().all()
                .statusCode(200);
    }

    @DisplayName("년도, 월별 직관 내역을 최신순으로 조회한다")
    @Test
    void findCheckInHistory_findAllWithYearMonthOrderByLatest() {
        // given
        Member fora = memberFactory.save(b -> b.team(kia));
        String accessToken = authFactory.getAccessTokenByMemberId(fora.getId(), Role.USER);

        int year = 2025;
        int month = 7;
        int day = 25;
        LocalDate date = LocalDate.of(year, month, day);
        Game game1 = gameFactory.save(builder ->
                builder.stadium(stadiumJamsil)
                        .date(date)
                        .gameState(GameState.COMPLETED)
                        .homeTeam(kt).homeScore(10).homeScoreBoard(TestFixture.getHomeScoreBoard())
                        .awayTeam(kia).awayScore(1).awayScoreBoard(TestFixture.getAwayScoreBoard()));
        Game game2 = gameFactory.save(builder ->
                builder.stadium(stadiumJamsil)
                        .date(date.plusDays(1))
                        .gameState(GameState.COMPLETED)
                        .homeTeam(kt).homeScore(10).homeScoreBoard(TestFixture.getHomeScoreBoard())
                        .awayTeam(kia).awayScore(1).awayScoreBoard(TestFixture.getAwayScoreBoard()));
        Game game3 = gameFactory.save(builder ->
                builder.stadium(stadiumJamsil)
                        .date(date.plusDays(2))
                        .gameState(GameState.COMPLETED)
                        .homeTeam(kt).homeScore(10).homeScoreBoard(TestFixture.getHomeScoreBoard())
                        .awayTeam(kia).awayScore(1).awayScoreBoard(TestFixture.getAwayScoreBoard()));
        Game previousGame = gameFactory.save(builder ->
                builder.stadium(stadiumJamsil)
                        .date(date.minusMonths(1))
                        .gameState(GameState.COMPLETED)
                        .homeTeam(kt).homeScore(10).homeScoreBoard(TestFixture.getHomeScoreBoard())
                        .awayTeam(kia).awayScore(1).awayScoreBoard(TestFixture.getAwayScoreBoard()));

        checkInFactory.save(b -> b.game(game1).team(kia).member(fora));
        checkInFactory.save(b -> b.game(game2).team(kia).member(fora));
        checkInFactory.save(b -> b.game(game3).team(kia).member(fora));
        checkInFactory.save(b -> b.game(previousGame).team(kia).member(fora));

        // when & then
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .queryParam("year", year)
                .queryParam("month", month)
                .queryParam("result", CheckInResultFilter.ALL)
                .queryParam("order", CheckInOrderFilter.LATEST)
                .when().get("/api/v1/check-ins/members")
                .then().log().all()
                .statusCode(200)
                .extract();

        CheckInHistoryResponse result = response.as(CheckInHistoryResponse.class);
        assertThat(result.checkInHistory()).hasSize(3);
        assertThat(result.checkInHistory()).extracting(CheckInGameParam::gameState)
                .containsOnly(GameState.COMPLETED);
    }

    @DisplayName("모든 직관 내역을 오래된순으로 조회한다")
    @Test
    void findCheckInHistory_findAllOrderByOldest() {
        // given
        Member fora = memberFactory.save(b -> b.team(kia));
        String accessToken = authFactory.getAccessTokenByMemberId(fora.getId(), Role.USER);

        int year = 2025;
        int month = 7;
        int day = 25;
        LocalDate date = LocalDate.of(year, month, day);
        Game game1 = gameFactory.save(builder ->
                builder.stadium(stadiumJamsil)
                        .date(date)
                        .gameState(GameState.COMPLETED)
                        .homeTeam(kt).homeScore(10).homeScoreBoard(TestFixture.getHomeScoreBoard())
                        .awayTeam(kia).awayScore(1).awayScoreBoard(TestFixture.getAwayScoreBoard()));
        Game game2 = gameFactory.save(builder ->
                builder.stadium(stadiumJamsil)
                        .date(date.plusDays(1))
                        .gameState(GameState.COMPLETED)
                        .homeTeam(kt).homeScore(10).homeScoreBoard(TestFixture.getHomeScoreBoard())
                        .awayTeam(kia).awayScore(1).awayScoreBoard(TestFixture.getAwayScoreBoard()));
        Game game3 = gameFactory.save(builder ->
                builder.stadium(stadiumJamsil)
                        .date(date.plusDays(2))
                        .gameState(GameState.COMPLETED)
                        .homeTeam(kt).homeScore(10).homeScoreBoard(TestFixture.getHomeScoreBoard())
                        .awayTeam(kia).awayScore(1).awayScoreBoard(TestFixture.getAwayScoreBoard()));

        checkInFactory.save(b -> b.game(game1).team(kia).member(fora));
        checkInFactory.save(b -> b.game(game2).team(kia).member(fora));
        checkInFactory.save(b -> b.game(game3).team(kia).member(fora));

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .queryParam("year", year)
                .queryParam("result", CheckInResultFilter.ALL)
                .queryParam("order", CheckInOrderFilter.OLDEST)
                .when().get("/api/v1/check-ins/members")
                .then().log().all()
                .statusCode(200);
    }

    @DisplayName("이긴 직관 내역을 최신순으로 조회한다")
    @Test
    void findCheckInWinHistory_findWinOrderByLatest() {
        // given
        Member fora = memberFactory.save(b -> b.team(kia));
        String accessToken = authFactory.getAccessTokenByMemberId(fora.getId(), Role.USER);

        int year = 2025;
        int month = 7;
        int day = 25;
        LocalDate date = LocalDate.of(year, month, day);
        Game game1 = gameFactory.save(builder ->
                builder.stadium(stadiumJamsil)
                        .date(date)
                        .gameState(GameState.COMPLETED)
                        .homeTeam(kt).homeScore(10).homeScoreBoard(TestFixture.getHomeScoreBoard())
                        .awayTeam(kia).awayScore(1).awayScoreBoard(TestFixture.getAwayScoreBoard()));
        Game game2 = gameFactory.save(builder ->
                builder.stadium(stadiumJamsil)
                        .date(date.plusDays(1))
                        .gameState(GameState.COMPLETED)
                        .homeTeam(kt).homeScore(10).homeScoreBoard(TestFixture.getHomeScoreBoard())
                        .awayTeam(kia).awayScore(1).awayScoreBoard(TestFixture.getAwayScoreBoard()));
        Game game3 = gameFactory.save(builder ->
                builder.stadium(stadiumJamsil)
                        .date(date.plusDays(2))
                        .gameState(GameState.COMPLETED)
                        .homeTeam(kia).homeScore(10).homeScoreBoard(TestFixture.getHomeScoreBoard())
                        .awayTeam(samsung).awayScore(1).awayScoreBoard(TestFixture.getAwayScoreBoard()));

        checkInFactory.save(b -> b.game(game1).team(kia).member(fora));
        checkInFactory.save(b -> b.game(game2).team(kia).member(fora));
        checkInFactory.save(b -> b.game(game3).team(kia).member(fora));

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .queryParam("year", year)
                .queryParam("result", CheckInResultFilter.WIN)
                .queryParam("order", CheckInOrderFilter.LATEST)
                .when().get("/api/v1/check-ins/members")
                .then().log().all()
                .statusCode(200);
    }

    @DisplayName("이긴 직관 내역을 오래된순으로 조회한다")
    @Test
    void findCheckInWinHistory_findWinOrderByOldest() {
        // given
        Member fora = memberFactory.save(b -> b.team(kia));
        String accessToken = authFactory.getAccessTokenByMemberId(fora.getId(), Role.USER);

        int year = 2025;
        int month = 7;
        int day = 25;
        LocalDate date = LocalDate.of(year, month, day);
        Game game1 = gameFactory.save(builder ->
                builder.stadium(stadiumJamsil)
                        .date(date)
                        .gameState(GameState.COMPLETED)
                        .homeTeam(kt).homeScore(10).homeScoreBoard(TestFixture.getHomeScoreBoard())
                        .awayTeam(kia).awayScore(1).awayScoreBoard(TestFixture.getAwayScoreBoard()));
        Game game2 = gameFactory.save(builder ->
                builder.stadium(stadiumJamsil)
                        .date(date.plusDays(1))
                        .gameState(GameState.COMPLETED)
                        .homeTeam(kt).homeScore(10).homeScoreBoard(TestFixture.getHomeScoreBoard())
                        .awayTeam(kia).awayScore(1).awayScoreBoard(TestFixture.getAwayScoreBoard()));
        Game game3 = gameFactory.save(builder ->
                builder.stadium(stadiumJamsil)
                        .date(date.plusDays(2))
                        .gameState(GameState.COMPLETED)
                        .homeTeam(kia).homeScore(10).homeScoreBoard(TestFixture.getHomeScoreBoard())
                        .awayTeam(samsung).awayScore(1).awayScoreBoard(TestFixture.getAwayScoreBoard()));

        checkInFactory.save(b -> b.game(game1).team(kia).member(fora));
        checkInFactory.save(b -> b.game(game2).team(kia).member(fora));
        checkInFactory.save(b -> b.game(game3).team(kia).member(fora));

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .queryParam("year", year)
                .queryParam("result", CheckInResultFilter.WIN)
                .queryParam("order", CheckInOrderFilter.OLDEST)
                .when().get("/api/v1/check-ins/members")
                .then().log().all()
                .statusCode(200);
    }

    @DisplayName("인증 여부를 조회한다")
    @Test
    void findCheckInStatus() {
        // given
        Member fora = memberFactory.save(b -> b.team(kia));
        String accessToken = authFactory.getAccessTokenByMemberId(fora.getId(), Role.USER);

        LocalDate date = LocalDate.of(2025, 7, 25);
        Game game = gameFactory.save(builder ->
                builder.stadium(stadiumJamsil)
                        .date(date)
                        .homeTeam(kt).homeScore(10).homeScoreBoard(TestFixture.getHomeScoreBoard())
                        .awayTeam(kia).awayScore(1).awayScoreBoard(TestFixture.getAwayScoreBoard()));

        checkInFactory.save(b -> b.game(game).team(kia).member(fora));

        // when
        CheckInStatusResponse actual = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .queryParam("date", date.toString())
                .when().get("/api/v1/check-ins/status")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(CheckInStatusResponse.class);

        // then
        assertThat(actual.isCheckIn()).isTrue();
    }

    @DisplayName("오늘 경기하는 모든 구장 별 팬 점유율을 조회한다")
    @Test
    void findFanRatesByStadiums() {
        // given
        Member fora = memberFactory.save(b -> b.team(kt).nickname("포라"));
        String accessToken = authFactory.getAccessTokenByMemberId(fora.getId(), Role.USER);

        LocalDate startDate = LocalDate.of(2025, 7, 25);
        Game gameAandB = gameFactory.save(
                b -> b.stadium(stadiumJamsil).homeTeam(kia).awayTeam(kt).date(startDate));
        Game gameCandD = gameFactory.save(
                b -> b.stadium(stadiumGocheok).homeTeam(lg).awayTeam(samsung).date(startDate));
        Game gameEandF = gameFactory.save(
                b -> b.stadium(stadiumIncheon).homeTeam(doosan).awayTeam(lotte).date(startDate));

        createCheckInsForGame(kia, gameAandB, 20);
        createCheckInsForGame(kt, gameAandB, 10);
        createCheckInsForGame(lg, gameCandD, 10);
        createCheckInsForGame(samsung, gameCandD, 4);
        createCheckInsForGame(doosan, gameEandF, 6);
        createCheckInsForGame(lotte, gameEandF, 1);

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .queryParam("date", TestFixture.getToday().toString())
                .when().get("/api/v1/check-ins/stadiums/fan-rates")
                .then().log().all()
                .statusCode(200);
    }

    @DisplayName("구장별 방문 횟수 조회 - 방문한 경기장이 없을 때")
    @Test
    void findStadiumCheckInCounts_noCheckIn() {
        // given
        Member member = memberFactory.save(MemberBuilder::build);
        String accessToken = authFactory.getAccessTokenByMemberId(member.getId(), Role.USER);

        StadiumCheckInCountsResponse expected = new StadiumCheckInCountsResponse(
                List.of(
                        new StadiumCheckInCountParam(1L, "광주", 0L),
                        new StadiumCheckInCountParam(2L, "잠실", 0L),
                        new StadiumCheckInCountParam(3L, "고척", 0L),
                        new StadiumCheckInCountParam(4L, "수원", 0L),
                        new StadiumCheckInCountParam(5L, "대구", 0L),
                        new StadiumCheckInCountParam(6L, "사직", 0L),
                        new StadiumCheckInCountParam(7L, "문학", 0L),
                        new StadiumCheckInCountParam(8L, "창원", 0L),
                        new StadiumCheckInCountParam(9L, "대전", 0L)
                )
        );

        // when
        StadiumCheckInCountsResponse actual = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .queryParam("year", 2025)
                .when().get("/api/v1/check-ins/stadiums/counts")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(StadiumCheckInCountsResponse.class);

        // then
        assertThat(actual).isEqualTo(expected);
    }

    @DisplayName("구장별 방문 횟수 조회 - 방문한 경기장이 있을 때")
    @Test
    void findStadiumCheckInCounts_hasCheckIn() {
        // given
        Member member = memberFactory.save(MemberBuilder::build);
        String accessToken = authFactory.getAccessTokenByMemberId(member.getId(), Role.USER);

        Game game1 = gameFactory.save(builder -> builder
                .date(TestFixture.getYesterday())
                .stadium(stadiumJamsil)
                .homeTeam(samsung)
                .awayTeam(doosan)
        );
        checkInFactory.save(builder -> builder.game(game1).member(member).team(samsung));

        StadiumCheckInCountsResponse expected = new StadiumCheckInCountsResponse(
                List.of(
                        new StadiumCheckInCountParam(1L, "광주", 0L),
                        new StadiumCheckInCountParam(2L, "잠실", 1L),
                        new StadiumCheckInCountParam(3L, "고척", 0L),
                        new StadiumCheckInCountParam(4L, "수원", 0L),
                        new StadiumCheckInCountParam(5L, "대구", 0L),
                        new StadiumCheckInCountParam(6L, "사직", 0L),
                        new StadiumCheckInCountParam(7L, "문학", 0L),
                        new StadiumCheckInCountParam(8L, "창원", 0L),
                        new StadiumCheckInCountParam(9L, "대전", 0L)
                )
        );

        // when
        StadiumCheckInCountsResponse actual = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .queryParam("year", 2025)
                .when().get("/api/v1/check-ins/stadiums/counts")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(StadiumCheckInCountsResponse.class);

        // then
        assertThat(actual).isEqualTo(expected);
    }

    private void createCheckInsForGame(Team team, Game game, int count) {
        for (int i = 0; i < count; i++) {
            Member member = memberFactory.save(b -> b.team(team));
            checkInFactory.save(b -> b.member(member).team(team).game(game));
        }
    }

    @DisplayName("year가 주어지지 않으면 전체 기간의 구장별 방문 횟수를 조회한다")
    @Test
    void findStadiumCheckInCounts_withoutYear() {
        // given
        Member member = memberFactory.save(MemberBuilder::build);
        String accessToken = authFactory.getAccessTokenByMemberId(member.getId(), Role.USER);

        // 2024년: 잠실 1회
        Game game2024 = gameFactory.save(builder -> builder
                .date(LocalDate.of(2024, 7, 10))
                .stadium(stadiumJamsil)
                .homeTeam(samsung)
                .awayTeam(doosan));
        checkInFactory.save(builder -> builder.game(game2024).member(member).team(samsung));

        // 2025년: 잠실 1회, 고척 1회
        Game game2025Jamsil = gameFactory.save(builder -> builder
                .date(LocalDate.of(2025, 7, 20))
                .stadium(stadiumJamsil)
                .homeTeam(samsung)
                .awayTeam(doosan));
        Game game2025Gocheok = gameFactory.save(builder -> builder
                .date(LocalDate.of(2025, 7, 21))
                .stadium(stadiumGocheok)
                .homeTeam(kia)
                .awayTeam(kt));
        checkInFactory.save(builder -> builder.game(game2025Jamsil).member(member).team(samsung));
        checkInFactory.save(builder -> builder.game(game2025Gocheok).member(member).team(kia));

        StadiumCheckInCountsResponse expected = new StadiumCheckInCountsResponse(
                List.of(
                        new StadiumCheckInCountParam(1L, "광주", 0L),
                        new StadiumCheckInCountParam(2L, "잠실", 2L), // 2024년 1회 + 2025년 1회
                        new StadiumCheckInCountParam(3L, "고척", 1L), // 2025년 1회
                        new StadiumCheckInCountParam(4L, "수원", 0L),
                        new StadiumCheckInCountParam(5L, "대구", 0L),
                        new StadiumCheckInCountParam(6L, "사직", 0L),
                        new StadiumCheckInCountParam(7L, "문학", 0L),
                        new StadiumCheckInCountParam(8L, "창원", 0L),
                        new StadiumCheckInCountParam(9L, "대전", 0L)));

        // when: year 파라미터 없이 요청
        StadiumCheckInCountsResponse actual = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .when().get("/api/v1/check-ins/stadiums/counts")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(StadiumCheckInCountsResponse.class);

        // then: 전체 기간 집계 확인
        assertThat(actual).isEqualTo(expected);
    }

    @DisplayName("year가 주어지지 않으면 전체 기간의 직관 내역을 조회한다")
    @Test
    void findCheckInHistory_withoutYear() {
        // given
        Member fora = memberFactory.save(b -> b.team(kia));
        String accessToken = authFactory.getAccessTokenByMemberId(fora.getId(), Role.USER);

        // 2024년 경기
        Game game2024_1 = gameFactory.save(builder -> builder.stadium(stadiumJamsil)
                .date(LocalDate.of(2024, 6, 15))
                .gameState(GameState.COMPLETED)
                .homeTeam(kt).homeScore(10).homeScoreBoard(TestFixture.getHomeScoreBoard())
                .awayTeam(kia).awayScore(1).awayScoreBoard(TestFixture.getAwayScoreBoard()));
        Game game2024_2 = gameFactory.save(builder -> builder.stadium(stadiumJamsil)
                .date(LocalDate.of(2024, 7, 25))
                .gameState(GameState.COMPLETED)
                .homeTeam(kt).homeScore(10).homeScoreBoard(TestFixture.getHomeScoreBoard())
                .awayTeam(kia).awayScore(1).awayScoreBoard(TestFixture.getAwayScoreBoard()));

        checkInFactory.save(b -> b.game(game2024_1).team(kia).member(fora));
        checkInFactory.save(b -> b.game(game2024_2).team(kia).member(fora));

        // 2025년 경기
        Game game2025_1 = gameFactory.save(builder -> builder.stadium(stadiumJamsil)
                .date(LocalDate.of(2025, 6, 10))
                .gameState(GameState.COMPLETED)
                .homeTeam(kt).homeScore(10).homeScoreBoard(TestFixture.getHomeScoreBoard())
                .awayTeam(kia).awayScore(1).awayScoreBoard(TestFixture.getAwayScoreBoard()));
        Game game2025_2 = gameFactory.save(builder -> builder.stadium(stadiumJamsil)
                .date(LocalDate.of(2025, 7, 15))
                .gameState(GameState.COMPLETED)
                .homeTeam(kt).homeScore(10).homeScoreBoard(TestFixture.getHomeScoreBoard())
                .awayTeam(kia).awayScore(1).awayScoreBoard(TestFixture.getAwayScoreBoard()));
        Game game2025_3 = gameFactory.save(builder -> builder.stadium(stadiumJamsil)
                .date(LocalDate.of(2025, 8, 5))
                .gameState(GameState.COMPLETED)
                .homeTeam(kt).homeScore(10).homeScoreBoard(TestFixture.getHomeScoreBoard())
                .awayTeam(kia).awayScore(1).awayScoreBoard(TestFixture.getAwayScoreBoard()));

        checkInFactory.save(b -> b.game(game2025_1).team(kia).member(fora));
        checkInFactory.save(b -> b.game(game2025_2).team(kia).member(fora));
        checkInFactory.save(b -> b.game(game2025_3).team(kia).member(fora));

        // when: year 파라미터 없이 요청
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .queryParam("result", CheckInResultFilter.ALL)
                .queryParam("order", CheckInOrderFilter.LATEST)
                .when().get("/api/v1/check-ins/members")
                .then().log().all()
                .statusCode(200)
                .extract();

        // then: 전체 기간(5경기) 조회 확인
        CheckInHistoryResponse result = response.as(CheckInHistoryResponse.class);
        assertThat(result.checkInHistory()).hasSize(5);
        assertThat(result.checkInHistory()).extracting(CheckInGameParam::gameState)
                .containsOnly(GameState.COMPLETED);
    }

    // ── 리뷰 조회 ──────────────────────────────────────────────────────────────

    @DisplayName("직관 경기 리뷰를 조회한다")
    @Test
    void findCheckInReview() {
        // given
        Member fora = memberFactory.save(b -> b.team(kia));
        String accessToken = authFactory.getAccessTokenByMemberId(fora.getId(), Role.USER);

        Game game = gameFactory.save(builder ->
                builder.stadium(stadiumJamsil)
                        .date(LocalDate.of(2025, 8, 10))
                        .gameState(GameState.COMPLETED)
                        .homeTeam(kt).homeScore(3).homeScoreBoard(TestFixture.getHomeScoreBoard())
                        .awayTeam(kia).awayScore(5).awayScoreBoard(TestFixture.getAwayScoreBoard()));
        com.yagubogu.checkin.domain.CheckIn checkIn = checkInFactory.save(b -> b.game(game).team(kia).member(fora));

        hitterRecordRepository.save(new GameHitterRecord(game, true, 1, "CF", "홈타자1", 4, 1, 0, 0));
        hitterRecordRepository.save(new GameHitterRecord(game, false, 1, "CF", "원정타자1", 4, 2, 1, 1));
        pitcherRecordRepository.save(new GamePitcherRecord(game, true, "홈투수1", "L", "6", 22, 85, 21, 5, 0, 2, 6, 5, 5));
        pitcherRecordRepository.save(new GamePitcherRecord(game, false, "원정투수1", "W", "7", 25, 90, 24, 3, 0, 1, 8, 3, 3));

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .when().get("/api/v1/check-ins/" + checkIn.getId() + "/review")
                .then().log().all()
                .statusCode(200);
    }

    @DisplayName("예외: 존재하지 않는 직관 기록의 리뷰를 조회하면 404가 발생한다")
    @Test
    void findCheckInReview_notFound() {
        // given
        Member fora = memberFactory.save(b -> b.team(kia));
        String accessToken = authFactory.getAccessTokenByMemberId(fora.getId(), Role.USER);
        long invalidCheckInId = 999999L;

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .when().get("/api/v1/check-ins/" + invalidCheckInId + "/review")
                .then().log().all()
                .statusCode(404);
    }

    // ── 메모 CRUD ──────────────────────────────────────────────────────────────

    @DisplayName("메모를 조회한다 - 메모 없는 경우 null 반환")
    @Test
    void getMemo_returnsNull_whenNoMemo() {
        // given
        Member fora = memberFactory.save(b -> b.team(kia));
        String accessToken = authFactory.getAccessTokenByMemberId(fora.getId(), Role.USER);

        Game game = gameFactory.save(b -> b.stadium(stadiumJamsil).date(LocalDate.of(2025, 9, 1))
                .homeTeam(kt).awayTeam(kia));
        com.yagubogu.checkin.domain.CheckIn checkIn = checkInFactory.save(b -> b.game(game).team(kia).member(fora));

        // when
        CheckInMemoResponse actual = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .when().get("/api/v1/check-ins/" + checkIn.getId() + "/memo")
                .then().log().all()
                .statusCode(200)
                .extract().as(CheckInMemoResponse.class);

        // then
        assertThat(actual.memo()).isNull();
    }

    @DisplayName("메모를 수정 후 조회한다")
    @Test
    void getMemo_afterUpdate() {
        // given
        Member fora = memberFactory.save(b -> b.team(kia));
        String accessToken = authFactory.getAccessTokenByMemberId(fora.getId(), Role.USER);

        Game game = gameFactory.save(b -> b.stadium(stadiumJamsil).date(LocalDate.of(2025, 9, 2))
                .homeTeam(kt).awayTeam(kia));
        com.yagubogu.checkin.domain.CheckIn checkIn = checkInFactory.save(b -> b.game(game).team(kia).member(fora));

        RestAssured.given()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .body(new com.yagubogu.checkin.dto.v1.UpdateCheckInMemoRequest("수정된 메모"))
                .when().put("/api/v1/check-ins/" + checkIn.getId() + "/memo");

        // when
        CheckInMemoResponse actual = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .when().get("/api/v1/check-ins/" + checkIn.getId() + "/memo")
                .then().log().all()
                .statusCode(200)
                .extract().as(CheckInMemoResponse.class);

        // then
        assertThat(actual.memo()).isEqualTo("수정된 메모");
    }

    @DisplayName("예외: 존재하지 않는 직관 기록의 메모를 조회하면 404가 발생한다")
    @Test
    void getMemo_notFound() {
        // given
        Member fora = memberFactory.save(b -> b.team(kia));
        String accessToken = authFactory.getAccessTokenByMemberId(fora.getId(), Role.USER);
        long invalidCheckInId = 999999L;

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .when().get("/api/v1/check-ins/" + invalidCheckInId + "/memo")
                .then().log().all()
                .statusCode(404);
    }

    // ── 이미지 CRUD ────────────────────────────────────────────────────────────

    @DisplayName("이미지 목록을 조회한다 - 이미지 없는 경우 빈 리스트 반환")
    @Test
    void getImages_returnsEmptyList() {
        // given
        Member fora = memberFactory.save(b -> b.team(kia));
        String accessToken = authFactory.getAccessTokenByMemberId(fora.getId(), Role.USER);

        Game game = gameFactory.save(b -> b.stadium(stadiumJamsil).date(LocalDate.of(2025, 9, 3))
                .homeTeam(kt).awayTeam(kia));
        com.yagubogu.checkin.domain.CheckIn checkIn = checkInFactory.save(b -> b.game(game).team(kia).member(fora));

        // when
        CheckInImagesResponse actual = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .when().get("/api/v1/check-ins/" + checkIn.getId() + "/images")
                .then().log().all()
                .statusCode(200)
                .extract().as(CheckInImagesResponse.class);

        // then
        assertThat(actual.images()).isEmpty();
    }

    @DisplayName("예외: 존재하지 않는 이미지를 삭제하면 404가 발생한다")
    @Test
    void deleteImage_notFound() {
        // given
        Member fora = memberFactory.save(b -> b.team(kia));
        String accessToken = authFactory.getAccessTokenByMemberId(fora.getId(), Role.USER);

        Game game = gameFactory.save(b -> b.stadium(stadiumJamsil).date(LocalDate.of(2025, 9, 4))
                .homeTeam(kt).awayTeam(kia));
        com.yagubogu.checkin.domain.CheckIn checkIn = checkInFactory.save(b -> b.game(game).team(kia).member(fora));
        long invalidImageId = 999999L;

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .when().delete("/api/v1/check-ins/" + checkIn.getId() + "/images/" + invalidImageId)
                .then().log().all()
                .statusCode(404);
    }

    @DisplayName("직관 내역 조회 시 메모와 이미지 URL 목록이 포함되지 않는다")
    @Test
    void findCheckInHistory_excludesMemoAndImageUrls() {
        // given
        Member fora = memberFactory.save(b -> b.team(kia));
        String accessToken = authFactory.getAccessTokenByMemberId(fora.getId(), Role.USER);

        Game game = gameFactory.save(b -> b.stadium(stadiumJamsil).date(LocalDate.of(2025, 9, 5))
                .gameState(GameState.COMPLETED)
                .homeTeam(kt).homeScore(2).homeScoreBoard(TestFixture.getHomeScoreBoard())
                .awayTeam(kia).awayScore(5).awayScoreBoard(TestFixture.getAwayScoreBoard()));
        com.yagubogu.checkin.domain.CheckIn checkIn = checkInFactory.save(b -> b.game(game).team(kia).member(fora));

        RestAssured.given()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .body(new com.yagubogu.checkin.dto.v1.UpdateCheckInMemoRequest("기록에 남길 메모"))
                .when().put("/api/v1/check-ins/" + checkIn.getId() + "/memo");

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .queryParam("year", 2025)
                .queryParam("result", CheckInResultFilter.ALL)
                .queryParam("order", CheckInOrderFilter.LATEST)
                .when().get("/api/v1/check-ins/members")
                .then().log().all()
                .statusCode(200)
                .extract();

        // then
        CheckInHistoryResponse historyResponse = response.as(CheckInHistoryResponse.class);
        assertThat(historyResponse.checkInHistory()).hasSize(1);

        Map<String, Object> historyItem = response.jsonPath().getMap("checkInHistory[0]");
        assertThat(historyItem).doesNotContainKeys("memo", "imageUrls");
    }
}
