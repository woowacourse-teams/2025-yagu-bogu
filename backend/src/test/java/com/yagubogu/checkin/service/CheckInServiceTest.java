package com.yagubogu.checkin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.yagubogu.auth.config.AuthTestConfig;
import com.yagubogu.checkin.domain.CheckInResultFilter;
import com.yagubogu.checkin.dto.CheckInCountsResponse;
import com.yagubogu.checkin.dto.CheckInGameResponse;
import com.yagubogu.checkin.dto.CheckInHistoryResponse;
import com.yagubogu.checkin.dto.CheckInStatusResponse;
import com.yagubogu.checkin.dto.CreateCheckInRequest;
import com.yagubogu.checkin.dto.FanRateByGameResponse;
import com.yagubogu.checkin.dto.FanRateResponse;
import com.yagubogu.checkin.dto.TeamFanRateResponse;
import com.yagubogu.checkin.dto.VictoryFairyRankingResponses;
import com.yagubogu.checkin.repository.CheckInRepository;
import com.yagubogu.game.domain.Game;
import com.yagubogu.game.domain.GameState;
import com.yagubogu.game.repository.GameRepository;
import com.yagubogu.global.exception.NotFoundException;
import com.yagubogu.member.domain.Member;
import com.yagubogu.member.repository.MemberRepository;
import com.yagubogu.stadium.domain.Stadium;
import com.yagubogu.stadium.repository.StadiumRepository;
import com.yagubogu.support.TestFixture;
import com.yagubogu.support.checkin.CheckInFactory;
import com.yagubogu.support.game.GameFactory;
import com.yagubogu.support.member.MemberFactory;
import com.yagubogu.team.domain.Team;
import com.yagubogu.team.repository.TeamRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = {
        "spring.sql.init.data-locations=classpath:test-data-team-stadium.sql"
})
@Import(AuthTestConfig.class)
@DataJpaTest
class CheckInServiceTest {

    private CheckInService checkInService;

    @Autowired
    private CheckInFactory checkInFactory;

    @Autowired
    private MemberFactory memberFactory;

    @Autowired
    private GameFactory gameFactory;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private CheckInRepository checkInRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private StadiumRepository stadiumRepository;

    @Autowired
    private GameRepository gameRepository;

    private Team kia, kt, lg, samsung, doosan, lotte;
    private Stadium stadiumJamsil, stadiumGocheok, stadiumIncheon;

    @BeforeEach
    void setUp() {
        checkInService = new CheckInService(checkInRepository, memberRepository, stadiumRepository, gameRepository);

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
    void findOccupancyRate() {
        // given
        Member member = memberFactory.save(builder -> builder.team(lotte));
        Game game = gameFactory.save(builder ->
                builder.stadium(stadiumJamsil)
                        .homeTeam(lotte).homeScore(10).homeScoreBoard(TestFixture.getHomeScoreBoard())
                        .awayTeam(kia).awayScore(1).awayScoreBoard(TestFixture.getAwayScoreBoard()));
        CreateCheckInRequest request = new CreateCheckInRequest(stadiumJamsil.getId(), game.getDate());

        // when & then
        assertThatCode(() -> checkInService.createCheckIn(member.getId(), request))
                .doesNotThrowAnyException();
    }

    @DisplayName("구장을 찾을 수 없으면 예외가 발생한다")
    @Test
    void createCheckIn_notFoundStadium() {
        // given
        long memberId = 1L;
        long invalidStadiumId = 999L;
        LocalDate date = TestFixture.getToday();
        CreateCheckInRequest request = new CreateCheckInRequest(invalidStadiumId, date);

        // when & then
        Assertions.assertThatThrownBy(() -> checkInService.createCheckIn(memberId, request))
                .isExactlyInstanceOf(NotFoundException.class)
                .hasMessage("Stadium is not found");
    }

    @DisplayName("경기를 찾을 수 없으면 예외가 발생한다")
    @Test
    void createCheckIn_notFoundGame() {
        // given
        long memberId = 1L;
        long stadiumId = 1L;
        LocalDate invalidDate = TestFixture.getInvalidDate();
        CreateCheckInRequest request = new CreateCheckInRequest(stadiumId, invalidDate);

        // when & then
        assertThatThrownBy(() -> checkInService.createCheckIn(memberId, request))
                .isExactlyInstanceOf(NotFoundException.class)
                .hasMessage("Game is not found");
    }

    @DisplayName("회원을 찾을 수 없으면 예외가 발생한다")
    @Test
    void createCheckIn_notFoundMember() {
        // given
        long invalidMemberId = 999L;
        Game game = gameFactory.save(builder -> builder.stadium(stadiumJamsil)
                .homeTeam(kia).awayTeam(kt).date(LocalDate.now()));
        CreateCheckInRequest request = new CreateCheckInRequest(stadiumJamsil.getId(), game.getDate());

        // when & then
        assertThatThrownBy(() -> checkInService.createCheckIn(invalidMemberId, request))
                .isExactlyInstanceOf(NotFoundException.class)
                .hasMessage("Member is not found");
    }

    @DisplayName("회원의 총 인증 횟수를 조회한다")
    @Test
    void findCheckInCounts() {
        // given
        Member member = memberFactory.save(builder -> builder.team(lotte));
        int year = 2025;
        int expectedSize = 7;
        LocalDate startDate = LocalDate.of(year, 7, 25);
        for (int i = 0; i < expectedSize; i++) {
            final int index = i;
            Game game = gameFactory.save(gameBuilder ->
                    gameBuilder.date(startDate.plusDays(index))
                            .stadium(stadiumJamsil)
                            .homeTeam(lotte).homeScore(10).homeScoreBoard(TestFixture.getHomeScoreBoard())
                            .awayTeam(kia).awayScore(1).awayScoreBoard(TestFixture.getAwayScoreBoard())
                            .gameState(GameState.COMPLETED)
            );
            checkInFactory.save(builder -> builder.team(lotte).member(member).game(game));
        }

        // when
        CheckInCountsResponse actual = checkInService.findCheckInCounts(member.getId(), year);

        // then
        assertThat(actual.checkInCounts()).isEqualTo(expectedSize);
    }

    @DisplayName("직관 인증 내역을 모두 조회한다")
    @Test
    void findCheckInHistory_allCheckInsGivenYear() {
        // given
        Member member = memberFactory.save(builder -> builder.team(lotte));
        long memberId = member.getId();
        int year = 2025;
        int expectedSize = 6;
        CheckInResultFilter filter = CheckInResultFilter.ALL;
        LocalDate startDate = LocalDate.of(year, 7, 25);
        for (int i = 0; i < expectedSize; i++) {
            final int index = i;
            Game game = gameFactory.save(gameBuilder ->
                    gameBuilder.date(startDate.plusDays(index))
                            .stadium(stadiumJamsil)
                            .homeTeam(lotte).homeScore(10).homeScoreBoard(TestFixture.getHomeScoreBoard())
                            .awayTeam(kia).awayScore(1).awayScoreBoard(TestFixture.getAwayScoreBoard())
                            .gameState(GameState.COMPLETED)
            );
            checkInFactory.save(builder -> builder.team(lotte).member(member).game(game));
        }

        // when
        CheckInHistoryResponse actual = checkInService.findCheckInHistory(memberId, year, filter);

        // then
        assertThat(actual.checkInHistory().size()).isEqualTo(expectedSize);
    }

    @DisplayName("직관 인증 내역이 인증 날짜 내림차순으로 정렬되어 반환된다")
    @Test
    void findCheckInHistory_sortsByCheckInDateDescending() {
        // given
        Member member = memberFactory.save(builder -> builder.team(kia));
        long memberId = member.getId();
        int year = 2025;
        CheckInResultFilter filter = CheckInResultFilter.ALL;
        List<Game> games = List.of(
                gameFactory.save(
                        b -> b.stadium(stadiumJamsil).homeTeam(kia).awayTeam(kt).date(LocalDate.of(2025, 7, 21))),
                gameFactory.save(
                        b -> b.stadium(stadiumIncheon).homeTeam(kia).awayTeam(samsung).date(LocalDate.of(2025, 7, 20))),
                gameFactory.save(
                        b -> b.stadium(stadiumGocheok).homeTeam(kia).awayTeam(kt).date(LocalDate.of(2025, 7, 18)))
        );
        games.forEach(game -> checkInFactory.save(b -> b.member(member).team(member.getTeam()).game(game)));

        // when
        CheckInHistoryResponse actual = checkInService.findCheckInHistory(memberId, year, filter);

        // then
        assertThat(actual.checkInHistory())
                .extracting(CheckInGameResponse::attendanceDate)
                .isSortedAccordingTo(Comparator.reverseOrder());
    }

    @DisplayName("승리 요정 랭킹 조회 - 승률, 직관 횟수, 닉네임 순 정렬되어 반환된다")
    @Test
    void findVictoryFairyRankings() {
        // given
        Member por = memberFactory.save(b -> b.team(kia).nickname("포르"));
        memberFactory.save(b -> b.team(kt).nickname("포라"));
        memberFactory.save(b -> b.team(lg).nickname("두리"));
        memberFactory.save(b -> b.team(kia).nickname("밍트"));
        memberFactory.save(b -> b.team(samsung).nickname("우가"));

        LocalDate startDate = LocalDate.of(2025, 7, 21);
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

        // when
        VictoryFairyRankingResponses actual = checkInService.findVictoryFairyRankings(por.getId());

        // then
        assertSoftly(softAssertions -> {
                    softAssertions.assertThat(actual.topRankings())
                            .extracting("nickname")
                            .containsExactly("밍트", "포르", "포라", "두리", "우가");
                    softAssertions.assertThat(actual.myRanking().nickname()).isEqualTo("포르");
                    softAssertions.assertThat(actual.myRanking().teamShortName()).isEqualTo("KIA");
                    softAssertions.assertThat(actual.myRanking().winPercent()).isEqualTo(100.0);
                }
        );
    }

    @DisplayName("승리 요정 랭킹 조회 중 회원이 인증한 정보가 없는 경우에 null이 아닌 회원 정보가 반환된다")
    @Test
    void findVictoryFairyRankings_notCheckInForMember() {
        // given
        Member fora = memberFactory.save(b -> b.team(kia).nickname("포라"));
        long memberId = fora.getId();

        // when
        VictoryFairyRankingResponses actual = checkInService.findVictoryFairyRankings(memberId);

        // then
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(actual.myRanking().ranking()).isEqualTo(0);
            softAssertions.assertThat(actual.myRanking().nickname()).isEqualTo(fora.getNickname());
            softAssertions.assertThat(actual.myRanking().teamShortName()).isEqualTo(fora.getTeam().getShortName());
            softAssertions.assertThat(actual.myRanking().winPercent()).isEqualTo(0.0);
        });
    }

    @DisplayName("요청받은 날짜에 인증을 했는지 검사한다")
    @Test
    void findCheckInStatus() {
        // given
        Member checkedInMember = memberFactory.save(b -> b.team(kt).nickname("포라"));
        Member notCheckedInMember = memberFactory.save(b -> b.team(kt).nickname("파이브라"));
        LocalDate date = LocalDate.of(2025, 7, 21);
        Game game = gameFactory.save(builder -> builder.stadium(stadiumJamsil)
                .homeTeam(kia)
                .awayTeam(kt)
                .homeScore(10)
                .awayScore(1)
                .homeScoreBoard(TestFixture.getHomeScoreBoard())
                .awayScoreBoard(TestFixture.getAwayScoreBoard())
                .date(date));

        checkInFactory.save(checkInBuilder -> checkInBuilder
                .member(checkedInMember)
                .team(checkedInMember.getTeam())
                .game(game)
        );

        // when
        CheckInStatusResponse actual1 = checkInService.findCheckInStatus(checkedInMember.getId(), date);
        CheckInStatusResponse actual2 = checkInService.findCheckInStatus(notCheckedInMember.getId(), date);

        // then
        assertThat(actual1.isCheckIn()).isTrue();
        assertThat(actual2.isCheckIn()).isFalse();
    }

    @DisplayName("직관 인증 내역 중 이긴 직관 내역을 모두 조회한다")
    @Test
    void findCheckInWinHistory_allCheckInWinsGivenYear() {
        // given
        Member por = memberFactory.save(b -> b.team(kia).nickname("포르"));
        long memberId = por.getId();
        int year = 2025;
        CheckInResultFilter filter = CheckInResultFilter.WIN;

        // setup win/lose history data
        LocalDate startDate = LocalDate.of(2025, 7, 25);
        // 승리 경기 3개
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
        // 패배 경기 3개
        gameFactory.save(b -> b.stadium(stadiumJamsil)
                .homeTeam(kt).homeScore(10)
                .awayTeam(kia).awayScore(1)
                .date(startDate.plusDays(3)));
        gameFactory.save(b -> b.stadium(stadiumJamsil)
                .homeTeam(lg).homeScore(10)
                .awayTeam(kia).awayScore(1)
                .date(startDate.plusDays(4)));
        gameFactory.save(b -> b.stadium(stadiumJamsil)
                .homeTeam(samsung).homeScore(10)
                .awayTeam(kia).awayScore(1)
                .date(startDate.plusDays(5)));

        gameRepository.findAll().forEach(game ->
                checkInFactory.save(b -> b.member(por).team(por.getTeam()).game(game))
        );

        int expectedSize = 3;

        // when
        CheckInHistoryResponse actual = checkInService.findCheckInHistory(memberId, year, filter);

        // then
        assertThat(actual.checkInHistory().size()).isEqualTo(expectedSize);
    }

    @DisplayName("직관 인증 내역 중 이긴 내역만 필터링되어 인증 날짜 내림차순으로 반환된다")
    @Test
    void findCheckInWinHistory_returnsOnlyWinsSortedByDateDescending() {
        // given
        Member por = memberFactory.save(b -> b.team(kia).nickname("포르"));
        long memberId = por.getId();
        int year = 2025;
        CheckInResultFilter filter = CheckInResultFilter.WIN;

        // setup win/lose history data
        LocalDate startDate = LocalDate.of(2025, 7, 25);
        // 승리 경기 3개
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
        // 패배 경기 3개
        gameFactory.save(b -> b.stadium(stadiumJamsil)
                .homeTeam(kt).homeScore(10)
                .awayTeam(kia).awayScore(1)
                .date(startDate.plusDays(3)));
        gameFactory.save(b -> b.stadium(stadiumJamsil)
                .homeTeam(lg).homeScore(10)
                .awayTeam(kia).awayScore(1)
                .date(startDate.plusDays(4)));
        gameFactory.save(b -> b.stadium(stadiumJamsil)
                .homeTeam(samsung).homeScore(10)
                .awayTeam(kia).awayScore(1)
                .date(startDate.plusDays(5)));

        gameRepository.findAll().forEach(game ->
                checkInFactory.save(b -> b.member(por).team(por.getTeam()).game(game))
        );

        // when
        CheckInHistoryResponse actual = checkInService.findCheckInHistory(memberId, year, filter);
        List<LocalDate> actualDates = actual.checkInHistory().stream()
                .map(CheckInGameResponse::attendanceDate)
                .toList();
        List<LocalDate> sortedDates = new ArrayList<>(actualDates);
        sortedDates.sort(Collections.reverseOrder());

        // then
        assertThat(actualDates).isEqualTo(sortedDates);
    }

    @DisplayName("오늘 경기 구장별 팬 점유율 조회 – 내 팀 경기 처음, 나머지 관중 수 많은 순 정렬")
    @Test
    void findFanRatesByGames() {
        // given
        Member fora = memberFactory.save(b -> b.team(kt).nickname("포라"));
        long memberId = fora.getId();
        LocalDate startDate = LocalDate.of(2025, 7, 21);
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

        FanRateResponse expected = new FanRateResponse(List.of(
                new FanRateByGameResponse(
                        30L,
                        new TeamFanRateResponse("KIA", "HT", 66.7),
                        new TeamFanRateResponse("KT", "KT", 33.3)),
                new FanRateByGameResponse(
                        14L,
                        new TeamFanRateResponse("LG", "LG", 71.4),
                        new TeamFanRateResponse("삼성", "SS", 28.6)),
                new FanRateByGameResponse(
                        7L,
                        new TeamFanRateResponse("두산", "OB", 85.7),
                        new TeamFanRateResponse("롯데", "LT", 14.3))
        ));

        // when
        FanRateResponse actual = checkInService.findFanRatesByGames(memberId, startDate);

        // then
        assertThat(actual.fanRateByGames()).containsExactlyElementsOf(expected.fanRateByGames());
    }

    private void createCheckInsForGame(Team team, Game game, int count) {
        for (int i = 0; i < count; i++) {
            Member member = memberFactory.save(b -> b.team(team));
            checkInFactory.save(b -> b.member(member).team(team).game(game));
        }
    }
}
