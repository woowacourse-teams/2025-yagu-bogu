package com.yagubogu.game.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.yagubogu.auth.config.AuthTestConfig;
import com.yagubogu.checkin.domain.CheckIn;
import com.yagubogu.game.domain.Game;
import com.yagubogu.game.dto.GameResponse;
import com.yagubogu.game.dto.GameWithCheckIn;
import com.yagubogu.game.dto.StadiumByGame;
import com.yagubogu.game.dto.TeamByGame;
import com.yagubogu.game.repository.GameRepository;
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

@Import(AuthTestConfig.class)
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

        List<GameWithCheckIn> expected = List.of(
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

    private GameWithCheckIn toDto(Game game, Long totalCheckIns, boolean isMine) {
        return new GameWithCheckIn(
                game.getId(),
                totalCheckIns,
                isMine,
                new StadiumByGame(game.getStadium().getId(), game.getStadium().getFullName()),
                new TeamByGame(game.getHomeTeam().getId(), game.getHomeTeam().getShortName(),
                        game.getHomeTeam().getTeamCode()),
                new TeamByGame(game.getAwayTeam().getId(), game.getAwayTeam().getShortName(),
                        game.getAwayTeam().getTeamCode())
        );
    }
}
