package com.yagubogu.checkin;

import com.yagubogu.auth.config.AuthTestConfig;
import com.yagubogu.checkin.domain.CheckInOrderFilter;
import com.yagubogu.checkin.domain.CheckInResultFilter;
import com.yagubogu.checkin.dto.CheckInCountsResponse;
import com.yagubogu.checkin.dto.CheckInStatusResponse;
import com.yagubogu.checkin.dto.CreateCheckInRequest;
import com.yagubogu.game.domain.Game;
import com.yagubogu.game.repository.GameRepository;
import com.yagubogu.global.config.JpaAuditingConfig;
import com.yagubogu.member.domain.Member;
import com.yagubogu.member.domain.Role;
import com.yagubogu.member.repository.MemberRepository;
import com.yagubogu.stadium.domain.Stadium;
import com.yagubogu.stadium.repository.StadiumRepository;
import com.yagubogu.support.TestFixture;
import com.yagubogu.support.auth.AuthFactory;
import com.yagubogu.support.checkin.CheckInFactory;
import com.yagubogu.support.game.GameFactory;
import com.yagubogu.support.member.MemberFactory;
import com.yagubogu.team.domain.Team;
import com.yagubogu.team.repository.TeamRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.util.List;
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

import static org.assertj.core.api.Assertions.assertThat;

@Import({AuthTestConfig.class, JpaAuditingConfig.class})
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class CheckInIntegrationTest {

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
    private MemberRepository memberRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private StadiumRepository stadiumRepository;

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

        stadiumJamsil = stadiumRepository.findById(1L).orElseThrow();
        stadiumGocheok = stadiumRepository.findById(2L).orElseThrow();
        stadiumIncheon = stadiumRepository.findById(3L).orElseThrow();
    }

    @DisplayName("인증을 저장한다")
    @Test
    void createCheckIn() {
        // given
        Member fora = memberFactory.save(b -> b.team(kia));
        String accessToken = authFactory.getAccessTokenByMemberId(fora.getId(), Role.USER);

        LocalDate date = LocalDate.of(2025, 7, 25);
        gameFactory.save(builder ->
                builder.stadium(stadiumJamsil)
                        .date(date)
                        .homeTeam(kt).homeScore(10).homeScoreBoard(TestFixture.getHomeScoreBoard())
                        .awayTeam(kia).awayScore(1).awayScoreBoard(TestFixture.getAwayScoreBoard()));

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .body(new CreateCheckInRequest(stadiumJamsil.getId(), date))
                .when().post("/api/check-ins")
                .then().log().all()
                .statusCode(201);
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
                .when().get("/api/check-ins/counts")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(CheckInCountsResponse.class);

        // then
        assertThat(actual.checkInCounts()).isEqualTo(3);
    }

    @DisplayName("예외: 인증할 때 구장이 없으면 예외가 발생한다")
    @Test
    void createCheckIn_notFoundStadium() {
        // given
        Member fora = memberFactory.save(b -> b.team(kia));
        String accessToken = authFactory.getAccessTokenByMemberId(fora.getId(), Role.USER);

        long invalidStadiumId = 999L;
        LocalDate date = LocalDate.of(2025, 7, 25);

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
        Member fora = memberFactory.save(b -> b.team(kia));
        String accessToken = authFactory.getAccessTokenByMemberId(fora.getId(), Role.USER);

        long stadiumId = kia.getId();
        LocalDate invalidDate = LocalDate.of(1000, 7, 21);

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .body(new CreateCheckInRequest(stadiumId, invalidDate))
                .when().post("/api/check-ins")
                .then().log().all()
                .statusCode(404);
    }

    @DisplayName("직관 내역을 최신순으로 조회한다")
    @Test
    void findCheckInHistory_findAllOrderByLatest() {
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

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .queryParam("year", 2025)
                .queryParam("result", CheckInResultFilter.ALL)
                .queryParam("order", CheckInOrderFilter.LATEST)
                .when().get("/api/check-ins/members")
                .then().log().all()
                .statusCode(200);
    }

    @DisplayName("모든 직관 내역을 오래된순으로 조회한다")
    @Test
    void findCheckInHistory_findAllOrderByOldest() {
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

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .queryParam("year", 2025)
                .queryParam("result", CheckInResultFilter.ALL)
                .queryParam("order", CheckInOrderFilter.OLDEST)
                .when().get("/api/check-ins/members")
                .then().log().all()
                .statusCode(200);
    }

    @DisplayName("이긴 직관 내역을 최신순으로 조회한다")
    @Test
    void findCheckInWinHistory_findWinOrderByLatest() {
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
                        .homeTeam(kia).homeScore(10).homeScoreBoard(TestFixture.getHomeScoreBoard())
                        .awayTeam(samsung).awayScore(1).awayScoreBoard(TestFixture.getAwayScoreBoard()));

        checkInFactory.save(b -> b.game(game1).team(kia).member(fora));
        checkInFactory.save(b -> b.game(game2).team(kia).member(fora));
        checkInFactory.save(b -> b.game(game3).team(kia).member(fora));

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .queryParam("year", 2025)
                .queryParam("result", CheckInResultFilter.WIN)
                .queryParam("order", CheckInOrderFilter.LATEST)
                .when().get("/api/check-ins/members")
                .then().log().all()
                .statusCode(200);
    }

    @DisplayName("이긴 직관 내역을 오래된순으로 조회한다")
    @Test
    void findCheckInWinHistory_findWinOrderByOldest() {
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
                        .homeTeam(kia).homeScore(10).homeScoreBoard(TestFixture.getHomeScoreBoard())
                        .awayTeam(samsung).awayScore(1).awayScoreBoard(TestFixture.getAwayScoreBoard()));

        checkInFactory.save(b -> b.game(game1).team(kia).member(fora));
        checkInFactory.save(b -> b.game(game2).team(kia).member(fora));
        checkInFactory.save(b -> b.game(game3).team(kia).member(fora));

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .queryParam("year", 2025)
                .queryParam("result", CheckInResultFilter.WIN)
                .queryParam("order", CheckInOrderFilter.OLDEST)
                .when().get("/api/check-ins/members")
                .then().log().all()
                .statusCode(200);
    }

    @DisplayName("승리 요정 랭킹을 조회한다")
    @Test
    void findVictoryFairyRankings() {
        // given
        Member fora = memberFactory.save(b -> b.team(kia).nickname("포라"));
        String accessToken = authFactory.getAccessTokenByMemberId(fora.getId(), Role.USER);

        memberFactory.save(b -> b.team(kt).nickname("포르"));
        memberFactory.save(b -> b.team(lg).nickname("두리"));
        memberFactory.save(b -> b.team(kia).nickname("밍트"));
        memberFactory.save(b -> b.team(samsung).nickname("우가"));

        LocalDate startDate = LocalDate.of(2025, 7, 25);
        gameFactory.save(b -> b.stadium(stadiumJamsil)
                .homeTeam(kia).homeScore(10)
                .awayTeam(kt).awayScore(1)
                .date(startDate));
        gameFactory.save(b -> b.stadium(stadiumJamsil)
                .homeTeam(kia).homeScore(10)
                .awayTeam(lg).awayScore(1)
                .date(startDate.plusDays(1)));
        gameFactory.save(b -> b.stadium(stadiumJamsil)
                .homeTeam(kia).homeScore(10)
                .awayTeam(samsung).awayScore(1)
                .date(startDate.plusDays(2)));
        gameFactory.save(b -> b.stadium(stadiumJamsil)
                .homeTeam(kt).homeScore(10)
                .awayTeam(lg).awayScore(1)
                .date(startDate.plusDays(3)));
        gameFactory.save(b -> b.stadium(stadiumJamsil)
                .homeTeam(kt).homeScore(10)
                .awayTeam(samsung).awayScore(1)
                .date(startDate.plusDays(4)));
        gameFactory.save(b -> b.stadium(stadiumJamsil)
                .homeTeam(lg).homeScore(10)
                .awayTeam(samsung).awayScore(1)
                .date(startDate.plusDays(5)));

        List<Member> members = memberRepository.findAll();
        List<Game> games = gameRepository.findAll();
        for (Member m : members) {
            for (Game g : games) {
                checkInFactory.save(b -> b.member(m).team(m.getTeam()).game(g));
            }
        }

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
                .when().get("/api/check-ins/status")
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
                .when().get("/api/check-ins/stadiums/fan-rates")
                .then().log().all()
                .statusCode(200);
    }

    private void createCheckInsForGame(Team team, Game game, int count) {
        for (int i = 0; i < count; i++) {
            Member member = memberFactory.save(b -> b.team(team));
            checkInFactory.save(b -> b.member(member).team(team).game(game));
        }
    }
}
