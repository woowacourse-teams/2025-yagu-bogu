package com.yagubogu.game;

import com.yagubogu.auth.config.AuthTestConfig;
import com.yagubogu.checkin.domain.CheckIn;
import com.yagubogu.game.domain.Game;
import com.yagubogu.game.domain.GameState;
import com.yagubogu.game.domain.InningHalf;
import com.yagubogu.game.dto.GameWithCheckInParam;
import com.yagubogu.game.dto.StadiumByGameParam;
import com.yagubogu.game.dto.TeamByGameParam;
import com.yagubogu.game.dto.v1.GameDatesResponse;
import com.yagubogu.game.dto.v1.GameResponse;
import com.yagubogu.game.dto.v1.LiveGamesResponse;
import com.yagubogu.game.dto.v1.LiveGamesResponse.CurrentPlayerRole;
import com.yagubogu.game.repository.GameRepository;
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
import com.yagubogu.support.member.MemberFactory;
import com.yagubogu.team.domain.Team;
import com.yagubogu.team.repository.TeamRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@Import({AuthTestConfig.class, JpaAuditingConfig.class})
public class GameE2eTest extends E2eTestBase {

    @LocalServerPort
    private int port;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private StadiumRepository stadiumRepository;

    @Autowired
    private GameFactory gameFactory;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private CheckInFactory checkInFactory;

    @Autowired
    private MemberFactory memberFactory;

    @Autowired
    private AuthFactory authFactory;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @DisplayName("경기하고 있는 모든 구장, 팀을 조회한다")
    @Test
    void findGamesByDate() {
        // given
        LocalDate date = TestFixture.getToday();

        Game game1 = makeGame(date, "HT", "LT", "잠실구장");
        Game game2 = makeGame(date, "WO", "HH", "고척돔");
        Game game3 = makeGame(date, "SK", "SS", "랜더스필드");

        Team team = getTeamByCode("SS");
        Member member = makeMember(team);
        String accessToken = authFactory.getAccessTokenByMemberId(member.getId(), Role.USER);

        // game1 등록
        makeCheckIn(game1, team, member);
        makeCheckIns(game1, team, 2);

        // game2 등록
        makeCheckIns(game2, team, 4);

        // game3
        makeCheckIns(game3, team, 5);

        List<GameWithCheckInParam> expected = List.of(
                toDto(game1, 3L, true),
                toDto(game2, 4L, false),
                toDto(game3, 5L, false)
        );

        // when
        GameResponse actual = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .queryParam("date", date.toString())
                .when().get("/api/v1/games")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(GameResponse.class);

        // then
        assertThat(actual.games()).containsExactlyInAnyOrderElementsOf(expected);
    }

    @DisplayName("예외: 미래 날짜를 조회하려고 하면 예외가 발생한다")
    @Test
    void findGamesByDate_WhenDateIsInFuture() {
        // given
        Team team = getTeamByCode("SS");
        Member member = makeMember(team);
        String accessToken = authFactory.getAccessTokenByMemberId(member.getId(), Role.USER);
        LocalDate invalidDate = LocalDate.of(3000, 12, 12);

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .queryParam("date", invalidDate.toString())
                .when().get("/api/v1/games")
                .then().log().all()
                .statusCode(422);
    }

    @DisplayName("월별 경기 있는 날짜 목록을 반환한다")
    @Test
    void findGameDatesByYearMonth() {
        // given
        LocalDate date1 = LocalDate.of(2025, 5, 3);
        LocalDate date2 = LocalDate.of(2025, 5, 10);
        makeGame(date1, "HT", "LT", "잠실구장");
        makeGame(date2, "WO", "HH", "고척돔");

        Member member = makeMember(getTeamByCode("SS"));
        String accessToken = authFactory.getAccessTokenByMemberId(member.getId(), Role.USER);

        // when
        GameDatesResponse actual = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .queryParam("yearMonth", "2025-05")
                .when().get("/api/v1/games/dates")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(GameDatesResponse.class);

        // then
        assertThat(actual.dates()).containsExactlyInAnyOrder(date1, date2);
    }

    @DisplayName("현장톡 화면에 당일 전체 경기의 점수와 실시간 상태를 반환한다")
    @Test
    void findLiveGames() {
        // given
        LocalDate today = LocalDate.now();
        Team homeTeam = getTeamByCode("HT");
        Team awayTeam = getTeamByCode("LT");
        Stadium stadium = stadiumRepository.findByShortName("잠실구장").orElseThrow();

        Game liveGame = gameFactory.save(builder -> builder
                .homeTeam(homeTeam)
                .awayTeam(awayTeam)
                .stadium(stadium)
                .date(today)
                .startAt(LocalTime.of(14, 0))
                .homeScore(3)
                .awayScore(2)
                .gameState(GameState.LIVE));
        liveGame.updateLiveGameCenterState("away", "최지훈", "home", "김태경", 5, InningHalf.TOP);
        liveGame.updateLiveBaseState(true, false, true, 1, 2, 0);
        liveGame.updateProbablePitchers("김태경", "최지훈");
        gameRepository.save(liveGame);

        Game incompleteLiveGame = gameFactory.save(builder -> builder
                .homeTeam(getTeamByCode("SK"))
                .awayTeam(getTeamByCode("NC"))
                .stadium(stadium)
                .date(today)
                .startAt(LocalTime.of(15, 0))
                .gameState(GameState.LIVE));

        Game scheduledGame = gameFactory.save(builder -> builder
                .homeTeam(getTeamByCode("WO"))
                .awayTeam(getTeamByCode("HH"))
                .stadium(stadiumRepository.findByShortName("고척돔").orElseThrow())
                .date(today)
                .startAt(LocalTime.of(18, 30))
                .gameState(GameState.SCHEDULED));
        scheduledGame.updateProbablePitchers("문동주", "하영민");
        gameRepository.save(scheduledGame);

        Game completedGame = gameFactory.save(builder -> builder
                .homeTeam(homeTeam)
                .awayTeam(awayTeam)
                .stadium(stadium)
                .date(today)
                .startAt(LocalTime.of(19, 0))
                .homeScore(5)
                .awayScore(4)
                .gameState(GameState.COMPLETED));
        completedGame.updateLiveGameCenterState("away", "직전타자", "home", "직전투수", 9, InningHalf.TOP);
        completedGame.updateLiveBaseState(false, false, false, 0, 0, 3);
        gameRepository.save(completedGame);

        Game canceledGame = gameFactory.save(builder -> builder
                .homeTeam(getTeamByCode("SS"))
                .awayTeam(getTeamByCode("OB"))
                .stadium(stadium)
                .date(today)
                .startAt(LocalTime.of(20, 0))
                .gameState(GameState.CANCELED));

        gameFactory.save(builder -> builder
                .homeTeam(homeTeam)
                .awayTeam(awayTeam)
                .stadium(stadium)
                .date(today.minusDays(1)));

        Member member = makeMember(getTeamByCode("SS"));
        String accessToken = authFactory.getAccessTokenByMemberId(member.getId(), Role.USER);

        // when
        LiveGamesResponse actual = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .when().get("/api/v1/games/live")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(LiveGamesResponse.class);

        // then
        assertThat(actual.games()).hasSize(5);

        LiveGamesResponse.LiveGameResponse actualLiveGame = actual.games().getFirst();
        assertThat(actualLiveGame.gameId()).isEqualTo(liveGame.getId());
        assertThat(actualLiveGame.gameState()).isEqualTo(GameState.LIVE);
        assertThat(actualLiveGame.homeTeam().code()).isEqualTo("HT");
        assertThat(actualLiveGame.homeTeam().currentPlayer()).isEqualTo("김태경");
        assertThat(actualLiveGame.homeTeam().currentPlayerRole()).isEqualTo(CurrentPlayerRole.PITCHER);
        assertThat(actualLiveGame.homeTeam().score()).isEqualTo(3);
        assertThat(actualLiveGame.awayTeam().code()).isEqualTo("LT");
        assertThat(actualLiveGame.awayTeam().currentPlayer()).isEqualTo("최지훈");
        assertThat(actualLiveGame.awayTeam().currentPlayerRole()).isEqualTo(CurrentPlayerRole.BATTER);
        assertThat(actualLiveGame.awayTeam().score()).isEqualTo(2);
        assertThat(actualLiveGame.liveState().inning()).isEqualTo(5);
        assertThat(actualLiveGame.liveState().inningHalf()).isEqualTo(InningHalf.TOP);
        assertThat(actualLiveGame.liveState().bases().firstBaseOccupied()).isTrue();
        assertThat(actualLiveGame.liveState().bases().secondBaseOccupied()).isFalse();
        assertThat(actualLiveGame.liveState().bases().thirdBaseOccupied()).isTrue();
        assertThat(actualLiveGame.liveState().count().balls()).isEqualTo(1);
        assertThat(actualLiveGame.liveState().count().strikes()).isEqualTo(2);
        assertThat(actualLiveGame.liveState().count().outs()).isZero();

        LiveGamesResponse.LiveGameResponse actualIncompleteLiveGame = actual.games().get(1);
        assertThat(actualIncompleteLiveGame.gameId()).isEqualTo(incompleteLiveGame.getId());
        assertThat(actualIncompleteLiveGame.gameState()).isEqualTo(GameState.LIVE);
        assertThat(actualIncompleteLiveGame.liveState()).isNull();

        LiveGamesResponse.LiveGameResponse actualScheduledGame = actual.games().get(2);
        assertThat(actualScheduledGame.gameId()).isEqualTo(scheduledGame.getId());
        assertThat(actualScheduledGame.gameState()).isEqualTo(GameState.SCHEDULED);
        assertThat(actualScheduledGame.homeTeam().currentPlayer()).isEqualTo("문동주");
        assertThat(actualScheduledGame.homeTeam().currentPlayerRole()).isEqualTo(CurrentPlayerRole.PITCHER);
        assertThat(actualScheduledGame.awayTeam().currentPlayer()).isEqualTo("하영민");
        assertThat(actualScheduledGame.awayTeam().currentPlayerRole()).isEqualTo(CurrentPlayerRole.PITCHER);
        assertThat(actualScheduledGame.liveState()).isNull();

        LiveGamesResponse.LiveGameResponse actualCompletedGame = actual.games().get(3);
        assertThat(actualCompletedGame.gameId()).isEqualTo(completedGame.getId());
        assertThat(actualCompletedGame.gameState()).isEqualTo(GameState.COMPLETED);
        assertThat(actualCompletedGame.homeTeam().currentPlayer()).isNull();
        assertThat(actualCompletedGame.awayTeam().currentPlayer()).isNull();
        assertThat(actualCompletedGame.liveState()).isNull();

        LiveGamesResponse.LiveGameResponse actualCanceledGame = actual.games().get(4);
        assertThat(actualCanceledGame.gameId()).isEqualTo(canceledGame.getId());
        assertThat(actualCanceledGame.gameState()).isEqualTo(GameState.CANCELED);
        assertThat(actualCanceledGame.homeTeam().currentPlayer()).isNull();
        assertThat(actualCanceledGame.homeTeam().currentPlayerRole()).isNull();
        assertThat(actualCanceledGame.awayTeam().currentPlayer()).isNull();
        assertThat(actualCanceledGame.awayTeam().currentPlayerRole()).isNull();
        assertThat(actualCanceledGame.liveState()).isNull();
    }

    @DisplayName("취소된 경기만 있는 날짜는 결과에서 제외된다")
    @Test
    void findGameDatesByYearMonth_excludesCanceled() {
        // given
        LocalDate canceledDate = LocalDate.of(2025, 5, 3);
        LocalDate scheduledDate = LocalDate.of(2025, 5, 10);
        makeGameWithState(canceledDate, "HT", "LT", "잠실구장", GameState.CANCELED);
        makeGame(scheduledDate, "WO", "HH", "고척돔");

        Member member = makeMember(getTeamByCode("SS"));
        String accessToken = authFactory.getAccessTokenByMemberId(member.getId(), Role.USER);

        // when
        GameDatesResponse actual = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .queryParam("yearMonth", "2025-05")
                .when().get("/api/v1/games/dates")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(GameDatesResponse.class);

        // then
        assertThat(actual.dates()).containsExactly(scheduledDate);
        assertThat(actual.dates()).doesNotContain(canceledDate);
    }

    private Game makeGame(LocalDate date, String homeCode, String awayCode, String stadiumShortName) {
        Team homeTeam = getTeamByCode(homeCode);
        Team awayTeam = getTeamByCode(awayCode);
        Stadium stadium = stadiumRepository.findByShortName(stadiumShortName).orElseThrow();

        return gameFactory.save(builder -> builder
                .homeTeam(homeTeam)
                .awayTeam(awayTeam)
                .stadium(stadium)
                .date(date)
        );
    }

    private Game makeGameWithState(
            LocalDate date,
            String homeCode,
            String awayCode,
            String stadiumShortName,
            GameState gameState
    ) {
        Team homeTeam = getTeamByCode(homeCode);
        Team awayTeam = getTeamByCode(awayCode);
        Stadium stadium = stadiumRepository.findByShortName(stadiumShortName).orElseThrow();

        return gameFactory.save(builder -> builder
                .homeTeam(homeTeam)
                .awayTeam(awayTeam)
                .stadium(stadium)
                .date(date)
                .gameState(gameState)
        );
    }

    private void makeCheckIns(Game game, Team team, int count) {
        makeMembers(count, team).forEach(member ->
                makeCheckIn(game, team, member)
        );
    }

    private CheckIn makeCheckIn(final Game game, final Team team, final Member member) {
        return checkInFactory.save(builder -> builder
                .game(game)
                .member(member)
                .team(team)
        );
    }

    private List<Member> makeMembers(int n, Team team) {
        return IntStream.range(0, n)
                .mapToObj(i -> makeMember(team))
                .toList();
    }

    private Member makeMember(Team team) {
        return memberFactory.save(b -> b.team(team));
    }


    private Team getTeamByCode(String code) {
        return teamRepository.findByTeamCode(code).orElseThrow();
    }

    private GameWithCheckInParam toDto(Game game, Long totalCheckIns, boolean isMine) {
        return new GameWithCheckInParam(
                game.getId(),
                totalCheckIns,
                isMine,
                new StadiumByGameParam(game.getStadium().getId(), game.getStadium().getFullName()),
                new TeamByGameParam(game.getHomeTeam().getId(), game.getHomeTeam().getShortName(),
                        game.getHomeTeam().getTeamCode()),
                new TeamByGameParam(game.getAwayTeam().getId(), game.getAwayTeam().getShortName(),
                        game.getAwayTeam().getTeamCode()),
                game.getStartAt()
        );
    }
}
