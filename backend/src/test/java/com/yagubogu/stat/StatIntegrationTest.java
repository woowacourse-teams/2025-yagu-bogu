package com.yagubogu.stat;

import static org.assertj.core.api.Assertions.assertThat;

import com.yagubogu.auth.config.AuthTestConfig;
import com.yagubogu.game.domain.Game;
import com.yagubogu.game.domain.GameState;
import com.yagubogu.game.domain.ScoreBoard;
import com.yagubogu.member.domain.Member;
import com.yagubogu.member.domain.Role;
import com.yagubogu.stadium.domain.Stadium;
import com.yagubogu.stadium.repository.StadiumRepository;
import com.yagubogu.stat.dto.AverageStatisticResponse;
import com.yagubogu.stat.dto.LuckyStadiumResponse;
import com.yagubogu.stat.dto.StatCountsResponse;
import com.yagubogu.stat.dto.WinRateResponse;
import com.yagubogu.support.auth.AuthFactory;
import com.yagubogu.support.checkin.CheckInFactory;
import com.yagubogu.support.game.GameFactory;
import com.yagubogu.support.member.MemberFactory;
import com.yagubogu.team.domain.Team;
import com.yagubogu.team.repository.TeamRepository;
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

@Import(AuthTestConfig.class)
@TestPropertySource(properties = {
        "spring.sql.init.data-locations=classpath:test-data-team-stadium.sql"
})
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class StatIntegrationTest {

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

    private String accessToken;

    private Team ht, lt, ss;
    private Stadium kia, lot, sam;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        ht = teamRepository.findByTeamCode("HT").orElseThrow();
        lt = teamRepository.findByTeamCode("LT").orElseThrow();
        ss = teamRepository.findByTeamCode("SS").orElseThrow();

        kia = stadiumRepository.findByShortName("챔피언스필드").orElseThrow();
        lot = stadiumRepository.findByShortName("사직구장").orElseThrow();
        sam = stadiumRepository.findByShortName("라이온즈파크").orElseThrow();
    }

    @DisplayName("승패무 횟수와 총 직관 횟수를 조회한다")
    @Test
    void findStatCounts() {
        // given: ht 즐겨찾기 멤버 + 2025년 6경기(ht 기준 5승 1무) + 체크인 6건
        Member member = memberFactory.save(b -> b.team(ht));
        accessToken = authFactory.getAccessTokenByMemberId(member.getId(), Role.USER);

        Game g1 = gameFactory.save(b -> b.stadium(kia)
                .homeTeam(ht).awayTeam(lt)
                .date(LocalDate.of(2025, 7, 16))
                .homeScore(10).awayScore(9)
                .gameState(GameState.COMPLETED));
        Game g2 = gameFactory.save(b -> b.stadium(lot)
                .homeTeam(lt).awayTeam(ht)
                .date(LocalDate.of(2025, 7, 17))
                .homeScore(5).awayScore(10)
                .gameState(GameState.COMPLETED));
        Game g3 = gameFactory.save(b -> b.stadium(kia)
                .homeTeam(ht).awayTeam(ss)
                .date(LocalDate.of(2025, 7, 18))
                .homeScore(9).awayScore(4)
                .gameState(GameState.COMPLETED));
        Game g4 = gameFactory.save(b -> b.stadium(sam)
                .homeTeam(ss).awayTeam(ht)
                .date(LocalDate.of(2025, 7, 19))
                .homeScore(3).awayScore(8)
                .gameState(GameState.COMPLETED));
        Game g5 = gameFactory.save(b -> b.stadium(kia)
                .homeTeam(ht).awayTeam(lt)
                .date(LocalDate.of(2025, 7, 20))
                .homeScore(7).awayScore(6)
                .gameState(GameState.COMPLETED));
        Game g6 = gameFactory.save(b -> b.stadium(lot)
                .homeTeam(lt).awayTeam(ht)
                .date(LocalDate.of(2025, 7, 21))
                .homeScore(5).awayScore(5)
                .gameState(GameState.COMPLETED)); // 무

        checkInFactory.save(b -> b.game(g1).member(member).team(ht));
        checkInFactory.save(b -> b.game(g2).member(member).team(ht));
        checkInFactory.save(b -> b.game(g3).member(member).team(ht));
        checkInFactory.save(b -> b.game(g4).member(member).team(ht));
        checkInFactory.save(b -> b.game(g5).member(member).team(ht));
        checkInFactory.save(b -> b.game(g6).member(member).team(ht));

        // when
        StatCountsResponse actual = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .queryParams("year", 2025)
                .when().get("/api/stats/counts")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(StatCountsResponse.class);

        // then
        assertThat(actual).isEqualTo(new StatCountsResponse(5, 1, 0, 6));
    }

    @DisplayName("직관 승률을 조회한다")
    @Test
    void findWinRate() {
        // given: ht 즐겨찾기 멤버 + 2025년 6경기(5승 1패) + 체크인 6건 → 83.3%
        Member member = memberFactory.save(b -> b.team(ht));
        accessToken = authFactory.getAccessTokenByMemberId(member.getId(), Role.USER);

        Game g1 = gameFactory.save(b -> b.stadium(kia)
                .homeTeam(ht).awayTeam(lt)
                .date(LocalDate.of(2025, 7, 10))
                .homeScore(8).awayScore(5)
                .gameState(GameState.COMPLETED));
        Game g2 = gameFactory.save(b -> b.stadium(kia)
                .homeTeam(ht).awayTeam(ss)
                .date(LocalDate.of(2025, 7, 11))
                .homeScore(7).awayScore(3)
                .gameState(GameState.COMPLETED));
        Game g3 = gameFactory.save(b -> b.stadium(kia)
                .homeTeam(ht).awayTeam(lt)
                .date(LocalDate.of(2025, 7, 12))
                .homeScore(5).awayScore(4)
                .gameState(GameState.COMPLETED));
        Game g4 = gameFactory.save(b -> b.stadium(lot)
                .homeTeam(lt).awayTeam(ht)
                .date(LocalDate.of(2025, 7, 13))
                .homeScore(4).awayScore(6)
                .gameState(GameState.COMPLETED)); // 승
        Game g5 = gameFactory.save(b -> b.stadium(lot)
                .homeTeam(lt).awayTeam(ht)
                .date(LocalDate.of(2025, 7, 14))
                .homeScore(7).awayScore(3)
                .gameState(GameState.COMPLETED)); // 패
        Game g6 = gameFactory.save(b -> b.stadium(sam)
                .homeTeam(ss).awayTeam(ht)
                .date(LocalDate.of(2025, 7, 15))
                .homeScore(2).awayScore(5)
                .gameState(GameState.COMPLETED)); // 승

        checkInFactory.save(b -> b.game(g1).member(member).team(ht));
        checkInFactory.save(b -> b.game(g2).member(member).team(ht));
        checkInFactory.save(b -> b.game(g3).member(member).team(ht));
        checkInFactory.save(b -> b.game(g4).member(member).team(ht));
        checkInFactory.save(b -> b.game(g5).member(member).team(ht));
        checkInFactory.save(b -> b.game(g6).member(member).team(ht));

        // when
        WinRateResponse actual = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .queryParams("year", 2025)
                .when().get("/api/stats/win-rate")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(WinRateResponse.class);

        // then
        assertThat(actual).isEqualTo(new WinRateResponse(83.3));
    }

    @DisplayName("예외: 관리자일 경우 직관 승률을 조회하면 예외가 발생한다")
    @Test
    void findWinRate_whenAdmin() {
        Member admin = memberFactory.save(b -> b.role(Role.ADMIN));
        accessToken = authFactory.getAccessTokenByMemberId(admin.getId(), Role.ADMIN);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .queryParams("year", 2025)
                .when().get("/api/stats/win-rate")
                .then().log().all()
                .statusCode(403);
    }

    @DisplayName("행운의 구장을 조회한다")
    @Test
    void findLuckyStadium() {
        // given: 챔피언스필드에서 최소 1승
        Member member = memberFactory.save(b -> b.team(ht));
        accessToken = authFactory.getAccessTokenByMemberId(member.getId(), Role.USER);

        Game g1 = gameFactory.save(b -> b.stadium(kia)
                .homeTeam(ht).awayTeam(lt)
                .date(LocalDate.of(2025, 7, 10))
                .homeScore(6).awayScore(3)
                .gameState(GameState.COMPLETED));
        checkInFactory.save(b -> b.game(g1)
                .member(member)
                .team(ht));

        // when
        LuckyStadiumResponse actual = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .queryParams("year", 2025)
                .when().get("/api/stats/lucky-stadiums")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(LuckyStadiumResponse.class);

        // then
        assertThat(actual).isEqualTo(new LuckyStadiumResponse("챔피언스필드"));
    }

    @DisplayName("평균 득, 실, 실책, 안타, 피안타 조회한다")
    @Test
    void findAverageStatistic() {
        // given: 3경기 간단 시드(서비스에서 소수1자리 반올림 가정 → 7.7, 5.3, 0.3, 12.0, 9.0)
        Member member = memberFactory.save(b -> b.team(ht));
        accessToken = authFactory.getAccessTokenByMemberId(member.getId(), Role.USER);

        Game g1 = gameFactory.save(b -> b.stadium(kia)
                .homeTeam(ht).awayTeam(lt)
                .date(LocalDate.of(2025, 7, 10))
                .homeScore(8).awayScore(5)
                .homeScoreBoard(new ScoreBoard(8, 12, 0, 0))
                .awayScoreBoard(new ScoreBoard(5, 9, 1, 0))
                .gameState(GameState.COMPLETED));
        Game g2 = gameFactory.save(b -> b.stadium(kia).homeTeam(lt).awayTeam(ht)
                .date(LocalDate.of(2025, 7, 11))
                .homeScore(4).awayScore(10)
                .homeScoreBoard(new ScoreBoard(4, 8, 0, 0))
                .awayScoreBoard(new ScoreBoard(10, 13, 0, 0))
                .gameState(GameState.COMPLETED));
        Game g3 = gameFactory.save(b -> b.stadium(kia)
                .homeTeam(ht).awayTeam(lt)
                .date(LocalDate.of(2025, 7, 12))
                .homeScore(5).awayScore(7)
                .homeScoreBoard(new ScoreBoard(5, 11, 1, 0))
                .awayScoreBoard(new ScoreBoard(7, 10, 0, 0))
                .gameState(GameState.COMPLETED));

        checkInFactory.save(b -> b.game(g1).member(member).team(ht));
        checkInFactory.save(b -> b.game(g2).member(member).team(ht));
        checkInFactory.save(b -> b.game(g3).member(member).team(ht));

        // when
        AverageStatisticResponse actual = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .when().get("/api/stats/me")
                .then().log().all()
                .statusCode(200)
                .extract().as(AverageStatisticResponse.class);

        // then
        assertThat(actual).isEqualTo(new AverageStatisticResponse(7.7, 5.3, 0.3, 12.0, 9.0));
    }
}
