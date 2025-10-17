package com.yagubogu.game;

import static org.assertj.core.api.Assertions.assertThat;

import com.yagubogu.auth.config.AuthTestConfig;
import com.yagubogu.checkin.domain.CheckIn;
import com.yagubogu.game.domain.Game;
import com.yagubogu.game.dto.v1.GameResponse;
import com.yagubogu.game.dto.GameWithCheckInParam;
import com.yagubogu.game.dto.StadiumByGameParam;
import com.yagubogu.game.dto.TeamByGameParam;
import com.yagubogu.global.config.JpaAuditingConfig;
import com.yagubogu.member.domain.Member;
import com.yagubogu.member.domain.Role;
import com.yagubogu.stadium.domain.Stadium;
import com.yagubogu.stadium.repository.StadiumRepository;
import com.yagubogu.support.E2eTestBase;
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
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;

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
                .when().get("/api/games")
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
                .when().get("/api/games")
                .then().log().all()
                .statusCode(422);
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
                        game.getAwayTeam().getTeamCode())
        );
    }
}
