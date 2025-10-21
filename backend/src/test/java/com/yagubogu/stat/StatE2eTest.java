package com.yagubogu.stat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.yagubogu.auth.config.AuthTestConfig;
import com.yagubogu.checkin.domain.CheckInType;
import com.yagubogu.game.domain.Game;
import com.yagubogu.game.domain.GameState;
import com.yagubogu.game.domain.ScoreBoard;
import com.yagubogu.global.config.JpaAuditingConfig;
import com.yagubogu.member.domain.Member;
import com.yagubogu.member.domain.Role;
import com.yagubogu.member.dto.v1.MemberFavoriteRequest;
import com.yagubogu.stadium.domain.Stadium;
import com.yagubogu.stadium.repository.StadiumRepository;
import com.yagubogu.stat.dto.OpponentWinRateTeamParam;
import com.yagubogu.stat.dto.v1.AverageStatisticResponse;
import com.yagubogu.stat.dto.v1.LuckyStadiumResponse;
import com.yagubogu.stat.dto.v1.OpponentWinRateResponse;
import com.yagubogu.stat.dto.v1.StatCountsResponse;
import com.yagubogu.stat.dto.v1.WinRateResponse;
import com.yagubogu.support.auth.AuthFactory;
import com.yagubogu.support.base.E2eTestBase;
import com.yagubogu.support.checkin.CheckInFactory;
import com.yagubogu.support.game.GameFactory;
import com.yagubogu.support.member.MemberFactory;
import com.yagubogu.team.domain.Team;
import com.yagubogu.team.repository.TeamRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;

@Import({AuthTestConfig.class, JpaAuditingConfig.class})
public class StatE2eTest extends E2eTestBase {

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
                .when().get("/api/v1/stats/counts")
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
                .when().get("/api/v1/stats/win-rate")
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
                .when().get("/api/v1/stats/win-rate")
                .then().log().all()
                .statusCode(403);
    }

    @DisplayName("팀 변경 시 승률이 현재 팀 기준으로 반영된다")
    @Test
    void findWinRate_changesWithFavoriteTeam() {
        // given: 초기 즐겨찾기 KIA → 승률 100%
        Team kiaTeam = ht; // HT
        Team doosanTeam = teamRepository.findByTeamCode("OB").orElseThrow();
        Member member = memberFactory.save(b -> b.team(kiaTeam));
        accessToken = authFactory.getAccessTokenByMemberId(member.getId(), Role.USER);

        LocalDate date = LocalDate.of(2025, 7, 22);
        Game winGame = gameFactory.save(b -> b
                .stadium(kia)
                .homeTeam(kiaTeam).awayTeam(ss)
                .date(date)
                .homeScore(6).awayScore(2)
                .gameState(GameState.COMPLETED)
        );
        checkInFactory.save(b -> b.game(winGame).member(member).team(kiaTeam));

        // when: HT 기준 승률 확인 → 100.0
        WinRateResponse winRateAsHT = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .queryParams("year", 2025)
                .when().get("/api/v1/stats/win-rate")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(WinRateResponse.class);
        assertThat(winRateAsHT.winRate()).isEqualTo(100.0);

        // 팀을 두산(OB)으로 변경
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .body(new MemberFavoriteRequest(doosanTeam.getTeamCode()))
                .when().patch("/api/v1/members/favorites")
                .then().log().all()
                .statusCode(200);

        // then: OB 기준 승률 확인 → 0.0 (현재 팀에서 인증한 체크인이 없음)
        WinRateResponse winRateAsOB = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .queryParams("year", 2025)
                .when().get("/api/v1/stats/win-rate")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(WinRateResponse.class);
        assertThat(winRateAsOB.winRate()).isEqualTo(0.0);

        // 다시 KIA(HT)로 변경
        ValidatableResponse validatableResponse = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .body(new MemberFavoriteRequest(kiaTeam.getTeamCode()))
                .when().patch("/api/v1/members/favorites")
                .then().log().all()
                .statusCode(200);

        // 최종: HT 기준 승률 확인 → 100.0으로 복원
        WinRateResponse winRateAgainAsHT = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .queryParams("year", 2025)
                .when().get("/api/v1/stats/win-rate")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(WinRateResponse.class);
        assertThat(winRateAgainAsHT.winRate()).isEqualTo(100.0);
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
                .when().get("/api/v1/stats/lucky-stadiums")
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
        Member member = memberFactory.save(b -> b.team(ht));
        accessToken = authFactory.getAccessTokenByMemberId(member.getId(), Role.USER);

        ScoreBoard homeScoreBoard = new ScoreBoard(8, 12, 0, 0,
                List.of("0", "1", "2", "0", "0", "2", "0", "0", "0", "-", "-", "-"));
        ScoreBoard awayScoreBoard = new ScoreBoard(5, 9, 1, 0,
                List.of("1", "0", "0", "2", "0", "0", "0", "0", "0", "-", "-", "-"));

        Game g1 = gameFactory.save(b -> b.stadium(kia)
                .homeTeam(ht).awayTeam(lt)
                .date(LocalDate.of(2025, 7, 10))
                .homeScore(8).awayScore(5)
                .homeScoreBoard(homeScoreBoard)
                .awayScoreBoard(awayScoreBoard)
                .gameState(GameState.COMPLETED));
        Game g2 = gameFactory.save(b -> b.stadium(kia).homeTeam(lt).awayTeam(ht)
                .date(LocalDate.of(2025, 7, 11))
                .homeScore(4).awayScore(10)
                .homeScoreBoard(new ScoreBoard(4, 8, 0, 0,
                        List.of("0", "1", "2", "0", "0", "2", "0", "0", "0", "-", "-", "-")))
                .awayScoreBoard(new ScoreBoard(10, 13, 0, 0,
                        List.of("1", "0", "0", "2", "0", "0", "0", "0", "0", "-", "-", "-")))
                .gameState(GameState.COMPLETED));
        Game g3 = gameFactory.save(b -> b.stadium(kia)
                .homeTeam(ht).awayTeam(lt)
                .date(LocalDate.of(2025, 7, 12))
                .homeScore(5).awayScore(7)
                .homeScoreBoard(new ScoreBoard(5, 11, 1, 0,
                        List.of("0", "1", "2", "0", "0", "2", "0", "0", "0", "-", "-", "-")))
                .awayScoreBoard(new ScoreBoard(7, 10, 0, 0,
                        List.of("1", "0", "0", "2", "0", "0", "0", "0", "0", "-", "-", "-")))
                .gameState(GameState.COMPLETED));

        checkInFactory.save(b -> b.game(g1).member(member).team(ht));
        checkInFactory.save(b -> b.game(g2).member(member).team(ht));
        checkInFactory.save(b -> b.game(g3).member(member).team(ht));

        // when
        AverageStatisticResponse actual = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .when().get("/api/v1/stats/me")
                .then().log().all()
                .statusCode(200)
                .extract().as(AverageStatisticResponse.class);

        // then
        assertThat(actual).isEqualTo(new AverageStatisticResponse(7.7, 5.3, 0.3, 12.0, 9.0));
    }

    @DisplayName("상대팀별 승률을 계산해 반환하며, 승률 내림차순 → name(String) 오름차순으로 정렬하고 미대결 팀은 0.0으로 포함한다 (무=0 규칙)")
    @Test
    void findOpponentWinRate_opponents_sorted_and_includes_unplayed() {
        // given
        Member member = memberFactory.save(b -> b.team(ht));
        accessToken = authFactory.getAccessTokenByMemberId(member.getId(), Role.USER);

        // SS와는 2전 2승 → 100.0
        Game s1 = gameFactory.save(b -> b.stadium(kia)
                .homeTeam(ht).awayTeam(ss)
                .date(LocalDate.of(2025, 7, 10))
                .homeScore(5).awayScore(3)
                .gameState(GameState.COMPLETED));
        Game s2 = gameFactory.save(b -> b.stadium(sam)
                .homeTeam(ss).awayTeam(ht)
                .date(LocalDate.of(2025, 7, 11))
                .homeScore(2).awayScore(4)
                .gameState(GameState.COMPLETED));

        // LT와는 2전 1승 1패 → 50.0
        Game l1 = gameFactory.save(b -> b.stadium(kia)
                .homeTeam(ht).awayTeam(lt)
                .date(LocalDate.of(2025, 7, 12))
                .homeScore(6).awayScore(2)
                .gameState(GameState.COMPLETED));
        Game l2 = gameFactory.save(b -> b.stadium(lot)
                .homeTeam(lt).awayTeam(ht)
                .date(LocalDate.of(2025, 7, 13))
                .homeScore(7).awayScore(1)
                .gameState(GameState.COMPLETED));

        // NC와는 1전 0승 1무 → 0.0 (무는 분모만 포함)
        Team nc = teamRepository.findByTeamCode("NC").orElseThrow();
        Game n1 = gameFactory.save(b -> b.stadium(kia)
                .homeTeam(ht).awayTeam(nc)
                .date(LocalDate.of(2025, 7, 14))
                .homeScore(4).awayScore(4)
                .gameState(GameState.COMPLETED));

        // 체크인(전부 즐겨찾기팀 HT로)
        checkInFactory.save(b -> b.game(s1).member(member).team(ht));
        checkInFactory.save(b -> b.game(s2).member(member).team(ht));
        checkInFactory.save(b -> b.game(l1).member(member).team(ht));
        checkInFactory.save(b -> b.game(l2).member(member).team(ht));
        checkInFactory.save(b -> b.game(n1).member(member).team(ht));

        // when
        OpponentWinRateResponse actual = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .queryParam("year", 2025)
                .when().get("/api/v1/stats/win-rate/opponents")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(OpponentWinRateResponse.class);

        // then
        assertSoftly(s -> {
            s.assertThat(actual.opponents()).hasSize(9);

            // 1위: SS(2-0-0, 100.0)
            OpponentWinRateTeamParam first = actual.opponents().get(0);
            s.assertThat(first.teamCode()).isEqualTo("SS");
            s.assertThat(first.wins()).isEqualTo(2);
            s.assertThat(first.losses()).isEqualTo(0);
            s.assertThat(first.draws()).isEqualTo(0);
            s.assertThat(first.winRate()).isEqualTo(100.0);

            // 2위: LT(1-1-0, 50.0)
            OpponentWinRateTeamParam second = actual.opponents().get(1);
            s.assertThat(second.teamCode()).isEqualTo("LT");
            s.assertThat(second.wins()).isEqualTo(1);
            s.assertThat(second.losses()).isEqualTo(1);
            s.assertThat(second.draws()).isEqualTo(0);
            s.assertThat(second.winRate()).isEqualTo(50.0);

            // NC(0-0-1, 0.0) 포함 검증
            OpponentWinRateTeamParam ncRes = actual.opponents().stream()
                    .filter(r -> r.teamCode().equals("NC"))
                    .findFirst().orElseThrow();
            s.assertThat(ncRes.wins()).isZero();
            s.assertThat(ncRes.losses()).isZero();
            s.assertThat(ncRes.draws()).isEqualTo(1);
            s.assertThat(ncRes.winRate()).isEqualTo(0.0);

            List<String> zeros = actual.opponents().stream()
                    .filter(r -> r.winRate() == 0.0)
                    .map(OpponentWinRateTeamParam::teamCode)
                    .toList();

            s.assertThat(zeros)
                    .containsExactlyInAnyOrder("KT", "LG", "NC", "SK", "OB", "WO", "HH");

            // 전체 정렬 검증: winRate desc → name asc
            List<OpponentWinRateTeamParam> sorted = actual.opponents().stream()
                    .sorted(Comparator
                            .comparing(OpponentWinRateTeamParam::winRate).reversed()
                            .thenComparing(OpponentWinRateTeamParam::name))
                    .toList();
            s.assertThat(actual.opponents()).containsExactlyElementsOf(sorted);
        });
    }

    @DisplayName("예외: 상대팀별 승률 조회시 응원팀이 없으면 422를 반환한다")
    @Test
    void findOpponentWinRate_memberWithoutTeam_notFound() {
        // given
        Member member = memberFactory.save(b -> b.team(null));
        accessToken = authFactory.getAccessTokenByMemberId(member.getId(), Role.USER);

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .queryParam("year", 2025)
                .when().get("/api/v1/stats/win-rate/opponents")
                .then().log().all()
                .statusCode(422);
    }

    @DisplayName("PastCheckIn과 CheckIn을 통합하여 승패무 횟수를 조회한다")
    @Test
    void findStatCounts_withPastCheckIn() {
        // given: ht 즐겨찾기 멤버 + CheckIn 3경기(2승 1무) + PastCheckIn 2경기(1승 1패)
        Member member = memberFactory.save(b -> b.team(ht));
        accessToken = authFactory.getAccessTokenByMemberId(member.getId(), Role.USER);

        // CheckIn 경기들
        Game g1 = gameFactory.save(b -> b.stadium(kia)
                .homeTeam(ht).awayTeam(lt)
                .date(LocalDate.of(2025, 7, 16))
                .homeScore(5).awayScore(3)
                .gameState(GameState.COMPLETED));
        checkInFactory.save(b -> b.game(g1).member(member).team(ht));

        Game g2 = gameFactory.save(b -> b.stadium(kia)
                .homeTeam(ht).awayTeam(lt)
                .date(LocalDate.of(2025, 7, 17))
                .homeScore(4).awayScore(4)
                .gameState(GameState.COMPLETED));
        checkInFactory.save(b -> b.game(g2).member(member).team(ht));

        Game g3 = gameFactory.save(b -> b.stadium(kia)
                .homeTeam(lt).awayTeam(ht)
                .date(LocalDate.of(2025, 7, 18))
                .homeScore(2).awayScore(6)
                .gameState(GameState.COMPLETED));
        checkInFactory.save(b -> b.game(g3).member(member).team(ht));

        // PastCheckIn 경기들
        Game g4 = gameFactory.save(b -> b.stadium(kia)
                .homeTeam(ht).awayTeam(lt)
                .date(LocalDate.of(2025, 6, 19))
                .homeScore(7).awayScore(5)
                .gameState(GameState.COMPLETED));
        checkInFactory.save(b -> b.game(g4).member(member).team(ht).checkInType(CheckInType.NON_LOCATION_CHECK_IN));

        Game g5 = gameFactory.save(b -> b.stadium(kia)
                .homeTeam(lt).awayTeam(ht)
                .date(LocalDate.of(2025, 6, 20))
                .homeScore(8).awayScore(3)
                .gameState(GameState.COMPLETED));
        checkInFactory.save(b -> b.game(g5).member(member).team(ht).checkInType(CheckInType.NON_LOCATION_CHECK_IN));

        // when
        StatCountsResponse actual = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .queryParams("year", 2025)
                .when().get("/api/v1/stats/counts")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(StatCountsResponse.class);

        // then: CheckIn(2승 0패 1무) + PastCheckIn(1승 1패 0무) = 3승 1패 1무
        assertThat(actual).isEqualTo(new StatCountsResponse(3, 1, 1, 5));
    }

    @DisplayName("PastCheckIn과 CheckIn을 통합하여 직관 승률을 조회한다")
    @Test
    void findWinRate_withPastCheckIn() {
        // given: CheckIn 3경기(2승 1패) + PastCheckIn 2경기(1승 1패)
        Member member = memberFactory.save(b -> b.team(ht));
        accessToken = authFactory.getAccessTokenByMemberId(member.getId(), Role.USER);

        // CheckIn
        Game g1 = gameFactory.save(b -> b.stadium(kia)
                .homeTeam(ht).awayTeam(lt)
                .date(LocalDate.of(2025, 7, 10))
                .homeScore(5).awayScore(3)
                .gameState(GameState.COMPLETED));
        checkInFactory.save(b -> b.game(g1).member(member).team(ht));

        Game g2 = gameFactory.save(b -> b.stadium(kia)
                .homeTeam(lt).awayTeam(ht)
                .date(LocalDate.of(2025, 7, 11))
                .homeScore(6).awayScore(3)
                .gameState(GameState.COMPLETED));
        checkInFactory.save(b -> b.game(g2).member(member).team(ht));

        Game g3 = gameFactory.save(b -> b.stadium(kia)
                .homeTeam(ht).awayTeam(lt)
                .date(LocalDate.of(2025, 7, 12))
                .homeScore(7).awayScore(2)
                .gameState(GameState.COMPLETED));
        checkInFactory.save(b -> b.game(g3).member(member).team(ht));

        // PastCheckIn
        Game g4 = gameFactory.save(b -> b.stadium(kia)
                .homeTeam(ht).awayTeam(lt)
                .date(LocalDate.of(2025, 6, 13))
                .homeScore(8).awayScore(5)
                .gameState(GameState.COMPLETED));
        checkInFactory.save(b -> b.game(g4).member(member).team(ht).checkInType(CheckInType.NON_LOCATION_CHECK_IN));

        Game g5 = gameFactory.save(b -> b.stadium(kia)
                .homeTeam(lt).awayTeam(ht)
                .date(LocalDate.of(2025, 6, 14))
                .homeScore(9).awayScore(4)
                .gameState(GameState.COMPLETED));
        checkInFactory.save(b -> b.game(g5).member(member).team(ht).checkInType(CheckInType.NON_LOCATION_CHECK_IN));

        // when
        WinRateResponse actual = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .queryParams("year", 2025)
                .when().get("/api/v1/stats/win-rate")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(WinRateResponse.class);

        // then: 총 3승 2패 = 60.0%
        assertThat(actual).isEqualTo(new WinRateResponse(60.0));
    }

    @DisplayName("PastCheckIn과 CheckIn을 통합하여 행운의 구장을 조회한다")
    @Test
    void findLuckyStadium_withPastCheckIn() {
        // given
        Member member = memberFactory.save(b -> b.team(ht));
        accessToken = authFactory.getAccessTokenByMemberId(member.getId(), Role.USER);

        // CheckIn: 챔피언스필드 1승
        Game g1 = gameFactory.save(b -> b.stadium(kia)
                .homeTeam(ht).awayTeam(lt)
                .date(LocalDate.of(2025, 7, 1))
                .homeScore(5).awayScore(3)
                .gameState(GameState.COMPLETED));
        checkInFactory.save(b -> b.game(g1).member(member).team(ht));

        // PastCheckIn: 챔피언스필드 1승
        Game g2 = gameFactory.save(b -> b.stadium(kia)
                .homeTeam(ht).awayTeam(lt)
                .date(LocalDate.of(2025, 6, 2))
                .homeScore(6).awayScore(4)
                .gameState(GameState.COMPLETED));
        checkInFactory.save(b -> b.game(g2).member(member).team(ht).checkInType(CheckInType.NON_LOCATION_CHECK_IN));

        // when
        LuckyStadiumResponse actual = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .queryParams("year", 2025)
                .when().get("/api/v1/stats/lucky-stadiums")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(LuckyStadiumResponse.class);

        // then: 챔피언스필드 2승 0패 = 100%
        assertThat(actual).isEqualTo(new LuckyStadiumResponse("챔피언스필드"));
    }

    @DisplayName("PastCheckIn과 CheckIn을 통합하여 평균 통계를 조회한다")
    @Test
    void findAverageStatistic_withPastCheckIn() {
        Member member = memberFactory.save(b -> b.team(ht));
        accessToken = authFactory.getAccessTokenByMemberId(member.getId(), Role.USER);

        // CheckIn: 1경기
        Game g1 = gameFactory.save(b -> b.stadium(kia)
                .homeTeam(ht).awayTeam(lt)
                .date(LocalDate.of(2025, 7, 10))
                .homeScore(6).awayScore(4)
                .homeScoreBoard(new ScoreBoard(6, 10, 1, 0,
                        List.of("0", "1", "2", "0", "0", "2", "0", "0", "0", "-", "-", "-")))
                .awayScoreBoard(new ScoreBoard(4, 8, 0, 0,
                        List.of("0", "1", "2", "0", "0", "2", "0", "0", "0", "-", "-", "-")))
                .gameState(GameState.COMPLETED));
        checkInFactory.save(b -> b.game(g1).member(member).team(ht));

        // PastCheckIn: 1경기
        Game g2 = gameFactory.save(b -> b.stadium(kia)
                .homeTeam(lt).awayTeam(ht)
                .date(LocalDate.of(2025, 6, 11))
                .homeScore(5).awayScore(8)
                .homeScoreBoard(new ScoreBoard(5, 9, 0, 0,
                        List.of("0", "1", "2", "0", "0", "2", "0", "0", "0", "-", "-", "-")))
                .awayScoreBoard(new ScoreBoard(8, 12, 1, 0,
                        List.of("1", "0", "0", "2", "0", "0", "0", "0", "0", "-", "-", "-")))
                .gameState(GameState.COMPLETED));
        checkInFactory.save(b -> b.game(g2).member(member).team(ht).checkInType(CheckInType.NON_LOCATION_CHECK_IN));

        // when
        AverageStatisticResponse actual = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .when().get("/api/v1/stats/me")
                .then().log().all()
                .statusCode(200)
                .extract().as(AverageStatisticResponse.class);

        // then: 평균 득점 = (6+8)/2 = 7.0, 평균 실점 = (4+5)/2 = 4.5, 평균 실책 = (1+1)/2 = 1.0, 평균 안타 = (10+12)/2 = 11.0, 평균 피안타 = (8+9)/2 = 8.5
        assertThat(actual).isEqualTo(new AverageStatisticResponse(7.0, 4.5, 1.0, 11.0, 8.5));
    }

    @DisplayName("PastCheckIn과 CheckIn을 통합하여 상대팀별 승률을 조회한다")
    @Test
    void findOpponentWinRate_withPastCheckIn() {
        // given
        Member member = memberFactory.save(b -> b.team(ht));
        accessToken = authFactory.getAccessTokenByMemberId(member.getId(), Role.USER);

        // CheckIn: LT와 1승
        Game g1 = gameFactory.save(b -> b.stadium(kia)
                .homeTeam(ht).awayTeam(lt)
                .date(LocalDate.of(2025, 7, 1))
                .homeScore(5).awayScore(3)
                .gameState(GameState.COMPLETED));
        checkInFactory.save(b -> b.game(g1).member(member).team(ht));

        // PastCheckIn: LT와 1패, SS와 1승
        Game g2 = gameFactory.save(b -> b.stadium(kia)
                .homeTeam(lt).awayTeam(ht)
                .date(LocalDate.of(2025, 6, 2))
                .homeScore(6).awayScore(4)
                .gameState(GameState.COMPLETED));
        checkInFactory.save(b -> b.game(g2).member(member).team(ht).checkInType(CheckInType.NON_LOCATION_CHECK_IN));

        Game g3 = gameFactory.save(b -> b.stadium(kia)
                .homeTeam(ht).awayTeam(ss)
                .date(LocalDate.of(2025, 6, 3))
                .homeScore(7).awayScore(2)
                .gameState(GameState.COMPLETED));
        checkInFactory.save(b -> b.game(g3).member(member).team(ht).checkInType(CheckInType.NON_LOCATION_CHECK_IN));

        // when
        var actual = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .queryParam("year", 2025)
                .when().get("/api/v1/stats/win-rate/opponents")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(OpponentWinRateResponse.class);

        // then
        assertSoftly(s -> {
            s.assertThat(actual.opponents()).hasSize(9);

            // SS: 1승 0패 = 100%
            var ssRes = actual.opponents().stream()
                    .filter(r -> r.teamCode().equals("SS"))
                    .findFirst().orElseThrow();
            s.assertThat(ssRes.wins()).isEqualTo(1);
            s.assertThat(ssRes.losses()).isEqualTo(0);
            s.assertThat(ssRes.winRate()).isEqualTo(100.0);

            // LT: 1승 1패 = 50%
            var ltRes = actual.opponents().stream()
                    .filter(r -> r.teamCode().equals("LT"))
                    .findFirst().orElseThrow();
            s.assertThat(ltRes.wins()).isEqualTo(1);
            s.assertThat(ltRes.losses()).isEqualTo(1);
            s.assertThat(ltRes.winRate()).isEqualTo(50.0);
        });
    }
}
