package com.yagubogu.game.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.yagubogu.auth.config.AuthTestConfig;
import com.yagubogu.checkin.domain.CheckIn;
import com.yagubogu.game.domain.Game;
import com.yagubogu.game.domain.ScoreBoard;
import com.yagubogu.game.dto.GameResultParam;
import com.yagubogu.game.dto.GameResultParam.ScoreBoardParam;
import com.yagubogu.game.dto.GameWithCheckInParam;
import com.yagubogu.game.dto.StadiumByGameParam;
import com.yagubogu.game.dto.TeamByGameParam;
import com.yagubogu.game.dto.v1.GameResponse;
import com.yagubogu.game.repository.GameRepository;
import com.yagubogu.global.config.JpaAuditingConfig;
import com.yagubogu.global.exception.NotFoundException;
import com.yagubogu.global.exception.UnprocessableEntityException;
import com.yagubogu.member.domain.Member;
import com.yagubogu.member.repository.MemberRepository;
import com.yagubogu.stadium.domain.Stadium;
import com.yagubogu.stadium.repository.StadiumRepository;
import com.yagubogu.support.TestFixture;
import com.yagubogu.support.checkin.CheckInFactory;
import com.yagubogu.support.game.GameFactory;
import com.yagubogu.support.member.MemberBuilder;
import com.yagubogu.support.member.MemberFactory;
import com.yagubogu.team.domain.Team;
import com.yagubogu.team.repository.TeamRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@Import({AuthTestConfig.class, JpaAuditingConfig.class})
@DataJpaTest
class GameServiceTest {

    private GameService gameService;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private MemberRepository memberRepository;

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


    @BeforeEach
    void setUp() {
        gameService = new GameService(gameRepository, memberRepository);
    }

    @DisplayName("오늘 경기하는 모든 구장, 팀, 인증 횟수, 내 인증 여부를 조회한다")
    @Test
    void findGamesByDate() {
        // given
        LocalDate date = TestFixture.getToday();

        Game game1 = makeGame(date, "HT", "LT", "잠실구장");
        Game game2 = makeGame(date, "WO", "HH", "고척돔");
        Game game3 = makeGame(date, "SK", "SS", "랜더스필드");

        Team team = getTeamByCode("SS");
        Member member = makeMember(team);
        long memberId = member.getId();

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
        GameResponse actual = gameService.findGamesByDate(date, memberId);

        // then
        assertThat(actual.games()).containsExactlyInAnyOrderElementsOf(expected);
    }

    @DisplayName("예외: 미래 날짜를 조회하려고 하면 예외가 발생한다")
    @Test
    void findGamesByDate_WhenDateIsInFuture() {
        // given
        Member member = memberFactory.save(MemberBuilder::build);
        Long memberId = member.getId();
        LocalDate invalidDate = LocalDate.now().plusDays(1);

        // when & then
        assertThatThrownBy(() -> gameService.findGamesByDate(invalidDate, memberId))
                .isExactlyInstanceOf(UnprocessableEntityException.class)
                .hasMessage("Cannot retrieve games for future dates");
    }

    @DisplayName("예외: 오늘 경기하는 모든 구장, 팀, 인증횟수, 내 인증 여부를 조회할 때 회원을 찾을 수 없으면 예외가 발생한다")
    @Test
    void findGamesByDate_notFoundMember() {
        // given
        long invalidMemberId = 999L;
        LocalDate date = TestFixture.getToday();

        // when & then
        assertThatThrownBy(() -> gameService.findGamesByDate(date, invalidMemberId))
                .isExactlyInstanceOf(NotFoundException.class)
                .hasMessage("Member is not found");
    }

    @DisplayName("끝난 게임의 스코어보드를 조회한다")
    @Test
    void findGameScoreBoard() {
        // given
        LocalDate date = TestFixture.getToday();
        Game game = makeGameWithScoreBoard(date, "HT", "LT", "잠실구장");
        long gameId = game.getId();

        GameResultParam expected = new GameResultParam(
                ScoreBoardParam.from(expectedHomeScoreBoard()),
                ScoreBoardParam.from(expectedAwayScoreBoard()),
                "이포라", "김롯데"
        );

        // when
        GameResultParam scoreBoard = gameService.findScoreBoard(gameId);

        // then
        assertThat(scoreBoard).isEqualTo(expected);
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

    private Game makeGameWithScoreBoard(LocalDate date, String homeCode, String awayCode, String stadiumShortName) {
        Team homeTeam = getTeamByCode(homeCode);
        Team awayTeam = getTeamByCode(awayCode);
        Stadium stadium = stadiumRepository.findByShortName(stadiumShortName).orElseThrow();
        ScoreBoard homeScoreBoard = new ScoreBoard(5, 8, 1, 3,
                List.of("0", "1", "2", "0", "0", "2", "0", "0", "0", "-", "-"));
        ScoreBoard awayScoreBoard = new ScoreBoard(3, 6, 2, 4,
                List.of("1", "0", "0", "2", "0", "0", "0", "0", "0", "-", "-"));
        String homePitcher = "이포라";
        String awayPitcher = "김롯데";

        return gameFactory.save(builder -> builder
                .homeTeam(homeTeam)
                .awayTeam(awayTeam)
                .stadium(stadium)
                .date(date)
                .homeScoreBoard(homeScoreBoard)
                .awayScoreBoard(awayScoreBoard)
                .homePitcher(homePitcher)
                .awayPitcher(awayPitcher)
        );
    }

    private ScoreBoard expectedHomeScoreBoard() {
        return new ScoreBoard(
                5, 8, 1, 3,
                List.of("0", "1", "2", "0", "0", "2", "0", "0", "0", "-", "-")
        );
    }

    private ScoreBoard expectedAwayScoreBoard() {
        return new ScoreBoard(
                3, 6, 2, 4,
                List.of("1", "0", "0", "2", "0", "0", "0", "0", "0", "-", "-")
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
