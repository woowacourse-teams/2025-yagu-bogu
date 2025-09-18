package com.yagubogu.checkin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.yagubogu.auth.config.AuthTestConfig;
import com.yagubogu.checkin.domain.CheckIn;
import com.yagubogu.checkin.domain.CheckInOrderFilter;
import com.yagubogu.checkin.domain.CheckInResultFilter;
import com.yagubogu.checkin.dto.CheckInCountsResponse;
import com.yagubogu.checkin.dto.CheckInGameResponse;
import com.yagubogu.checkin.dto.CheckInHistoryResponse;
import com.yagubogu.checkin.dto.CheckInStatusResponse;
import com.yagubogu.checkin.dto.CreateCheckInRequest;
import com.yagubogu.checkin.dto.FanRateByGameResponse;
import com.yagubogu.checkin.dto.FanRateResponse;
import com.yagubogu.checkin.dto.StadiumCheckInCountResponse;
import com.yagubogu.checkin.dto.StadiumCheckInCountsResponse;
import com.yagubogu.checkin.dto.TeamFanRateResponse;
import com.yagubogu.checkin.dto.TeamFilter;
import com.yagubogu.checkin.dto.VictoryFairyRank;
import com.yagubogu.checkin.dto.VictoryFairyRankingResponses;
import com.yagubogu.checkin.dto.VictoryFairyRankingResponses.VictoryFairyRankingResponse;
import com.yagubogu.checkin.repository.CheckInRepository;
import com.yagubogu.game.domain.Game;
import com.yagubogu.game.domain.GameState;
import com.yagubogu.game.repository.GameRepository;
import com.yagubogu.global.config.JpaAuditingConfig;
import com.yagubogu.global.exception.NotFoundException;
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
import java.util.ArrayList;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@Import({AuthTestConfig.class, JpaAuditingConfig.class})
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

        stadiumJamsil = stadiumRepository.findById(2L).orElseThrow();
        stadiumGocheok = stadiumRepository.findById(3L).orElseThrow();
        stadiumIncheon = stadiumRepository.findById(4L).orElseThrow();
    }

    @DisplayName("인증을 저장한다")
    @Test
    void findOccupancyRate() {
        // given
        Member member = memberFactory.save(builder -> builder.team(lotte));
        Game game = gameFactory.save(builder ->
                builder.stadium(stadiumJamsil)
                        .homeTeam(lotte).homeScore(10).homeScoreBoard(TestFixture.getHomeScoreBoardAbout(10))
                        .awayTeam(kia).awayScore(1).awayScoreBoard(TestFixture.getAwayScoreBoardAbout(1)));
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
                            .homeTeam(lotte).homeScoreBoard(TestFixture.getHomeScoreBoardAbout(10))
                            .awayTeam(kia).awayScoreBoard(TestFixture.getAwayScoreBoardAbout(1))
                            .gameState(GameState.COMPLETED)
            );
            checkInFactory.save(builder -> builder.team(lotte).member(member).game(game));
        }

        // when
        CheckInCountsResponse actual = checkInService.findCheckInCounts(member.getId(), year);

        // then
        assertThat(actual.checkInCounts()).isEqualTo(expectedSize);
    }

    @DisplayName("직관 인증 내역을 모두 최신순으로 조회한다")
    @Test
    void findCheckInHistory_allCheckInsGivenYearOrderByLatest() {
        // given
        Member member = memberFactory.save(builder -> builder.team(lotte));
        long memberId = member.getId();
        int year = 2025;
        LocalDate startDate = LocalDate.of(year, 7, 25);
        CheckInResultFilter resultFilter = CheckInResultFilter.ALL;
        CheckInOrderFilter orderFilter = CheckInOrderFilter.LATEST;
        List<CheckIn> savedCheckIns = new ArrayList<>();

        makeGames(startDate, savedCheckIns, member);

        // when
        CheckInHistoryResponse actual = checkInService.findCheckInHistory(memberId, year, resultFilter, orderFilter);

        // then
        assertThat(actual.checkInHistory()).hasSize(4) // 먼저 승리한 경기 4개만 필터링되었는지 확인
                .extracting(
                        CheckInGameResponse::attendanceDate,
                        r -> r.homeTeam().name(),
                        r -> r.homeScoreBoard().getRuns(),
                        r -> r.awayTeam().name(),
                        r -> r.awayScoreBoard().getRuns()
                ).containsExactly(
                        tuple(startDate.plusDays(3), "롯데", 10, "KIA", 1),
                        tuple(startDate.plusDays(2), "롯데", 10, "KIA", 10),
                        tuple(startDate.plusDays(1), "롯데", 1, "KIA", 10),
                        tuple(startDate, "롯데", 10, "KIA", 1)
                );
    }

    @DisplayName("직관 인증 내역을 모두 오래된순으로 조회한다")
    @Test
    void findCheckInHistory_allCheckInsGivenYearOrderByOldest() {
        // given
        Member member = memberFactory.save(builder -> builder.team(lotte));
        long memberId = member.getId();
        int year = 2025;
        LocalDate startDate = LocalDate.of(year, 7, 25);
        CheckInResultFilter resultFilter = CheckInResultFilter.ALL;
        CheckInOrderFilter orderFilter = CheckInOrderFilter.OLDEST;
        List<CheckIn> savedCheckIns = new ArrayList<>();

        makeGames(startDate, savedCheckIns, member);

        // when
        CheckInHistoryResponse actual = checkInService.findCheckInHistory(memberId, year, resultFilter, orderFilter);

        // then
        assertThat(actual.checkInHistory()).hasSize(4)
                .extracting(
                        CheckInGameResponse::attendanceDate,
                        r -> r.homeTeam().name(),
                        r -> r.homeScoreBoard().getRuns(),
                        r -> r.awayTeam().name(),
                        r -> r.awayScoreBoard().getRuns()
                ).containsExactly(
                        tuple(startDate, "롯데", 10, "KIA", 1),
                        tuple(startDate.plusDays(1), "롯데", 1, "KIA", 10),
                        tuple(startDate.plusDays(2), "롯데", 10, "KIA", 10),
                        tuple(startDate.plusDays(3), "롯데", 10, "KIA", 1)
                );
    }

    @DisplayName("직관 인증 내역 중 이긴 내역만 필터링되어 최신순으로 반환된다")
    @Test
    void findCheckInWinHistory_returnsOnlyWinsOrderByLatest() {
        // given
        Member por = memberFactory.save(b -> b.team(kia).nickname("포르"));
        long memberId = por.getId();
        int year = 2025;
        LocalDate startDate = LocalDate.of(2025, 7, 25);

        CheckInResultFilter resultFilter = CheckInResultFilter.WIN;
        CheckInOrderFilter orderFilter = CheckInOrderFilter.LATEST;
        List<CheckIn> savedCheckIns = new ArrayList<>();
        // 승리 경기 3개
        makeWinningGames(startDate, savedCheckIns, por);

        // 패배 경기 3개
        makeLosingGames(startDate, savedCheckIns, por);

        // when
        CheckInHistoryResponse actual = checkInService.findCheckInHistory(memberId, year, resultFilter, orderFilter);

        // then
        assertThat(actual.checkInHistory()).hasSize(3) // 먼저 승리한 경기 3개만 필터링되었는지 확인
                .extracting(
                        CheckInGameResponse::attendanceDate,
                        r -> r.homeTeam().name(),
                        r -> r.homeScoreBoard().getRuns(),
                        r -> r.awayTeam().name(),
                        r -> r.awayScoreBoard().getRuns()
                ).containsExactly(
                        tuple(startDate.plusDays(2), "KIA", 4, "삼성", 0),
                        tuple(startDate.plusDays(1), "KIA", 5, "LG", 4),
                        tuple(startDate, "KIA", 10, "KT", 1)
                );
    }

    @DisplayName("직관 인증 내역 중 이긴 내역만 필터링되어 오래된순으로 반환된다")
    @Test
    void findCheckInWinHistory_returnsOnlyWinsOrderByOldest() {
        // given
        Member por = memberFactory.save(b -> b.team(kia).nickname("포르"));
        long memberId = por.getId();
        int year = 2025;
        LocalDate startDate = LocalDate.of(2025, 7, 25);

        CheckInResultFilter resultFilter = CheckInResultFilter.WIN;
        CheckInOrderFilter orderFilter = CheckInOrderFilter.OLDEST;
        List<CheckIn> savedCheckIns = new ArrayList<>();
        // 승리 경기 3개
        makeWinningGames(startDate, savedCheckIns, por);

        // 패배 경기 3개
        makeLosingGames(startDate, savedCheckIns, por);

        // when
        CheckInHistoryResponse actual = checkInService.findCheckInHistory(memberId, year, resultFilter, orderFilter);

        // then
        assertThat(actual.checkInHistory()).hasSize(3) // 먼저 승리한 경기 3개만 필터링되었는지 확인
                .extracting(
                        CheckInGameResponse::attendanceDate,
                        r -> r.homeTeam().name(),
                        r -> r.homeScoreBoard().getRuns(),
                        r -> r.awayTeam().name(),
                        r -> r.awayScoreBoard().getRuns()
                ).containsExactly(
                        tuple(startDate, "KIA", 10, "KT", 1),
                        tuple(startDate.plusDays(1), "KIA", 5, "LG", 4),
                        tuple(startDate.plusDays(2), "KIA", 4, "삼성", 0)
                );

    }

    @DisplayName("승리 요정 랭킹 조회 - 베이즈 정리로 정렬되어 반환된다")
    @Test
    void findVictoryFairyRankings() {
        // given
        Member por = memberFactory.save(b -> b.team(kia).nickname("포르"));
        Member fora = memberFactory.save(b -> b.team(kt).nickname("포라"));
        Member duri = memberFactory.save(b -> b.team(lg).nickname("두리"));
        Member mint = memberFactory.save(b -> b.team(kia).nickname("밍트"));
        Member uga = memberFactory.save(b -> b.team(samsung).nickname("우가"));

        LocalDate startDate = LocalDate.of(2025, 7, 21);
        Game g1 = gameFactory.save(b -> b.stadium(stadiumJamsil)
                .homeTeam(kia).homeScore(10)
                .awayTeam(kt).awayScore(1)
                .homeScoreBoard(TestFixture.getHomeScoreBoardAbout(10))
                .awayScoreBoard(TestFixture.getAwayScoreBoardAbout(1))
                .date(startDate)
                .gameState(GameState.COMPLETED)
        );
        Game g2 = gameFactory.save(b -> b.stadium(stadiumJamsil)
                .homeTeam(kia).homeScore(10)
                .awayTeam(lg).awayScore(1)
                .homeScoreBoard(TestFixture.getHomeScoreBoardAbout(10))
                .awayScoreBoard(TestFixture.getAwayScoreBoardAbout(1))
                .date(startDate.plusDays(1))
                .gameState(GameState.COMPLETED)

        );
        Game g3 = gameFactory.save(b -> b.stadium(stadiumJamsil)
                .homeTeam(kia).homeScore(10)
                .awayTeam(samsung).awayScore(1)
                .homeScoreBoard(TestFixture.getHomeScoreBoardAbout(10))
                .awayScoreBoard(TestFixture.getAwayScoreBoardAbout(1))
                .date(startDate.plusDays(2))
                .gameState(GameState.COMPLETED)
        );
        Game g4 = gameFactory.save(b -> b.stadium(stadiumJamsil)
                .homeTeam(kt).homeScore(10)
                .awayTeam(lg).awayScore(1)
                .homeScoreBoard(TestFixture.getHomeScoreBoardAbout(10))
                .awayScoreBoard(TestFixture.getAwayScoreBoardAbout(1))
                .date(startDate.plusDays(3))
                .gameState(GameState.COMPLETED)
        );
        Game g5 = gameFactory.save(b -> b.stadium(stadiumJamsil)
                .homeTeam(kt).homeScore(10)
                .awayTeam(samsung).awayScore(1)
                .homeScoreBoard(TestFixture.getHomeScoreBoardAbout(10))
                .awayScoreBoard(TestFixture.getAwayScoreBoardAbout(1))
                .date(startDate.plusDays(4))
                .gameState(GameState.COMPLETED)
        );
        Game g6 = gameFactory.save(b -> b.stadium(stadiumJamsil)
                .homeTeam(lg).homeScore(10)
                .awayTeam(samsung).awayScore(1)
                .homeScoreBoard(TestFixture.getHomeScoreBoardAbout(10))
                .awayScoreBoard(TestFixture.getAwayScoreBoardAbout(1))
                .date(startDate.plusDays(5))
                .gameState(GameState.COMPLETED)
        );
        Game g7 = gameFactory.save(b -> b.stadium(stadiumJamsil)
                .homeTeam(lg)
                .awayTeam(samsung)
                .homeScoreBoard(null)
                .awayScoreBoard(null)
                .date(startDate.plusDays(6))
                .gameState(GameState.SCHEDULED)
        );

        // 체크인: "응원팀이 출전한 경기"에만 체크인시켜 승률/표본이 의도대로 형성되게 함
        // KIA 팬: 밍트(3경기 전부), 포르(2경기) → 둘 다 100%지만 표본 수로 밍트가 상위
        checkInFavorite(mint, g1, g2, g3);
        checkInFavorite(por, g1, g2);

        // KT 팬: 포라(2경기 중 2승 → 100%)
        checkInFavorite(fora, g4, g5);

        // LG 팬: 두리(2경기 중 0승 → 0%)
        checkInFavorite(duri, g1, g4); // LG는 위 셋에서 모두 패배

        // 삼성 팬: 우가(3경기 중 0승 → 0%)
        checkInFavorite(uga, g3, g5, g6, g7);

        // when
        VictoryFairyRankingResponses actual = checkInService.findVictoryFairyRankings(por.getId(), TeamFilter.ALL,
                2025);

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

    @DisplayName("승리 요정 랭킹 조회 - 베이즈 정리로 정렬되어 반환된다")
    @Test
    void findVictoryFairyRankings_filterByTeam() {
        // given
        Member mint = memberFactory.save(b -> b.team(kia).nickname("밍트"));
        Member por = memberFactory.save(b -> b.team(kia).nickname("포르"));
        Member duri = memberFactory.save(b -> b.team(lg).nickname("두리"));

        LocalDate startDate = LocalDate.of(2025, 7, 21);
        Game g1 = gameFactory.save(b -> b.stadium(stadiumJamsil)
                .homeTeam(kia).homeScore(10)
                .awayTeam(kt).awayScore(1)
                .homeScoreBoard(TestFixture.getHomeScoreBoardAbout(10))
                .awayScoreBoard(TestFixture.getAwayScoreBoardAbout(1))
                .date(startDate)
                .gameState(GameState.COMPLETED)
        );
        Game g2 = gameFactory.save(b -> b.stadium(stadiumJamsil)
                .homeTeam(kia).homeScore(10)
                .awayTeam(lg).awayScore(1)
                .homeScoreBoard(TestFixture.getHomeScoreBoardAbout(10))
                .awayScoreBoard(TestFixture.getAwayScoreBoardAbout(1))
                .date(startDate.plusDays(1))
                .gameState(GameState.COMPLETED)

        );
        Game g4 = gameFactory.save(b -> b.stadium(stadiumJamsil)
                .homeTeam(kt).homeScore(10)
                .awayTeam(lg).awayScore(1)
                .homeScoreBoard(TestFixture.getHomeScoreBoardAbout(10))
                .awayScoreBoard(TestFixture.getAwayScoreBoardAbout(1))
                .date(startDate.plusDays(3))
                .gameState(GameState.COMPLETED)
        );

        checkInFavorite(mint, g1);
        checkInFavorite(por, g1, g2);
        checkInFavorite(duri, g1, g4);

        // when
        VictoryFairyRankingResponses actual = checkInService.findVictoryFairyRankings(por.getId(), TeamFilter.HT, 2025);

        // then
        assertSoftly(softAssertions -> {
                    softAssertions.assertThat(actual.topRankings())
                            .extracting("nickname")
                            .containsExactly("포르", "밍트");
                    softAssertions.assertThat(actual.myRanking().nickname()).isEqualTo("포르");
                    softAssertions.assertThat(actual.myRanking().teamShortName()).isEqualTo("KIA");
                    softAssertions.assertThat(actual.myRanking().winPercent()).isEqualTo(100.0);
                }
        );
    }

    @DisplayName("승리 요정 랭킹 조회 - 취소된 경기가 존재하는 경우 승률에 집계되지 않는다")
    @Test
    void findVictoryFairyRankings_winRate() {
        // given
        Member mint = memberFactory.save(b -> b.team(kia).nickname("밍트"));
        LocalDate startDate = LocalDate.of(2025, 7, 21);

        Game g1 = gameFactory.save(b -> b.stadium(stadiumJamsil)
                .homeTeam(kia).homeScore(10)
                .awayTeam(kt).awayScore(1)
                .homeScoreBoard(TestFixture.getHomeScoreBoardAbout(10))
                .awayScoreBoard(TestFixture.getAwayScoreBoardAbout(1))
                .date(startDate)
                .gameState(GameState.COMPLETED)
        );
        Game g2 = gameFactory.save(b -> b.stadium(stadiumJamsil)
                .homeTeam(kia).homeScore(1)
                .awayTeam(lg).awayScore(10)
                .homeScoreBoard(TestFixture.getHomeScoreBoardAbout(10))
                .awayScoreBoard(TestFixture.getAwayScoreBoardAbout(1))
                .date(startDate.plusDays(1))
                .gameState(GameState.CANCELED)
        );

        checkInFavorite(mint, g1, g2);

        // when
        VictoryFairyRankingResponse myRanking = checkInService.findVictoryFairyRankings(mint.getId(),
                TeamFilter.ALL,
                startDate.getYear()).myRanking();

        // then
        assertThat(myRanking.winPercent()).isEqualTo(100.0);
    }

    private void checkInFavorite(Member member, Game... games) {
        for (Game g : games) {
            boolean participates =
                    g.getHomeTeam().getId().equals(member.getTeam().getId()) ||
                            g.getAwayTeam().getId().equals(member.getTeam().getId());
            if (participates) {
                checkInFactory.save(b -> b.member(member).team(member.getTeam()).game(g));
            }
        }
    }

    @DisplayName("승리 요정 랭킹 조회 중 회원이 인증한 정보가 없는 경우에 null이 아닌 회원 정보가 반환된다")
    @Test
    void findVictoryFairyRankings_notCheckInForMember() {
        // given
        Member fora = memberFactory.save(b -> b.team(kia).nickname("포라"));
        long memberId = fora.getId();

        // when
        VictoryFairyRankingResponses actual = checkInService.findVictoryFairyRankings(memberId, TeamFilter.ALL, 2025);

        // then
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(actual.myRanking().ranking()).isEqualTo(0);
            softAssertions.assertThat(actual.myRanking().nickname()).isEqualTo(fora.getNickname());
            softAssertions.assertThat(actual.myRanking().teamShortName()).isEqualTo(fora.getTeam().getShortName());
            softAssertions.assertThat(actual.myRanking().winPercent()).isEqualTo(0.0);
        });
    }

    @DisplayName("회원이 응원하는 팀의 경기를 한번도 관람하지 않은 경우 승률이 0이다")
    @Test
    void findVictoryFairyRankings_noFavoriteCheckIn() {
        // given
        Member por = memberFactory.save(b -> b.team(samsung).nickname("포르"));

        LocalDate startDate = LocalDate.of(2025, 7, 21);
        Game game = gameFactory.save(b -> b.stadium(stadiumJamsil)
                .homeTeam(kia)
                .awayTeam(kt)
                .homeScoreBoard(TestFixture.getHomeScoreBoardAbout(10))
                .awayScoreBoard(TestFixture.getAwayScoreBoardAbout(1))
                .date(startDate));
        checkInFactory.save(builder -> builder
                .team(samsung)
                .member(por)
                .game(game)
        );

        // when
        VictoryFairyRankingResponses actual = checkInService.findVictoryFairyRankings(por.getId(), TeamFilter.ALL,
                2025);

        // then
        assertSoftly(softAssertions -> {
                    softAssertions.assertThat(actual.topRankings())
                            .extracting("nickname")
                            .containsExactly("포르");
                    softAssertions.assertThat(actual.myRanking().nickname()).isEqualTo("포르");
                    softAssertions.assertThat(actual.myRanking().teamShortName()).isEqualTo("삼성");
                    softAssertions.assertThat(actual.myRanking().winPercent()).isEqualTo(0.0);
                }
        );
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
                .homeScoreBoard(TestFixture.getHomeScoreBoardAbout(10))
                .homeScoreBoard(TestFixture.getHomeScoreBoardAbout(1))
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

    @DisplayName("오늘 경기 구장별 팬 점유율 조회 – 경기가 없으면 null이 아닌 빈 리스트를 반환한다")
    @Test
    void findFanRatesByGames_returnsEmptyListWhenNoMyTeamGames() {
        // given
        Member fora = memberFactory.save(b -> b.team(kt).nickname("포라"));
        long memberId = fora.getId();
        LocalDate startDate = LocalDate.of(2025, 7, 25);

        FanRateResponse expected = new FanRateResponse(List.of());

        // when
        FanRateResponse actual = checkInService.findFanRatesByGames(memberId, startDate);

        // then
        assertThat(actual.fanRateByGames()).containsExactlyElementsOf(expected.fanRateByGames());
    }

    @DisplayName("오늘 경기 구장별 팬 점유율 조회 - 직관한 사람이 없는 경기도 반환한다")
    @Test
    void findFanRatesByGames_noCheckInsButReturnGames() {
        // given
        Member wooga = memberFactory.save(b -> b.team(doosan).nickname("우가"));
        long memberId = wooga.getId();

        LocalDate gameDate = LocalDate.of(2025, 7, 25);
        gameFactory.save(
                b -> b.stadium(stadiumJamsil).homeTeam(kia).awayTeam(kt).date(gameDate));
        gameFactory.save(
                b -> b.stadium(stadiumIncheon).homeTeam(doosan).awayTeam(lotte).date(gameDate));

        List<FanRateByGameResponse> expected = List.of(
                new FanRateByGameResponse(
                        0L,
                        new TeamFanRateResponse("두산", "OB", 0),
                        new TeamFanRateResponse("롯데", "LT", 0)),
                new FanRateByGameResponse(
                        0L,
                        new TeamFanRateResponse("KIA", "HT", 0),
                        new TeamFanRateResponse("KT", "KT", 0))
        );

        // when
        FanRateResponse actual = checkInService.findFanRatesByGames(memberId, gameDate);

        // then
        assertThat(actual.fanRateByGames()).containsExactlyElementsOf(expected);
    }

    @DisplayName("구장별 방문 횟수 조회 - 방문한 경기장이 없을 때")
    @Test
    void findStadiumCheckInCounts_noCheckIn() {
        // given
        Member member = memberFactory.save(MemberBuilder::build);

        StadiumCheckInCountsResponse expected = new StadiumCheckInCountsResponse(
                List.of(
                        new StadiumCheckInCountResponse(1L, "광주", 0L),
                        new StadiumCheckInCountResponse(2L, "잠실", 0L),
                        new StadiumCheckInCountResponse(3L, "고척", 0L),
                        new StadiumCheckInCountResponse(4L, "수원", 0L),
                        new StadiumCheckInCountResponse(5L, "대구", 0L),
                        new StadiumCheckInCountResponse(6L, "부산", 0L),
                        new StadiumCheckInCountResponse(7L, "인천", 0L),
                        new StadiumCheckInCountResponse(8L, "마산", 0L),
                        new StadiumCheckInCountResponse(9L, "대전", 0L)
                )
        );

        // when
        StadiumCheckInCountsResponse actual = checkInService.findStadiumCheckInCounts(member.getId(),
                2025);

        // then
        assertThat(actual).isEqualTo(expected);
    }

    @DisplayName("구장별 방문 횟수 조회 - 방문한 경기장이 있을 때")
    @Test
    void findStadiumCheckInCounts_hasCheckIn() {
        // given
        Member member = memberFactory.save(MemberBuilder::build);
        Game game = gameFactory.save(builder -> builder
                .date(TestFixture.getYesterday())
                .stadium(stadiumGocheok)
                .homeTeam(samsung)
                .awayTeam(doosan)
        );
        checkInFactory.save(builder -> builder.game(game).member(member).team(samsung));

        StadiumCheckInCountsResponse expected = new StadiumCheckInCountsResponse(
                List.of(
                        new StadiumCheckInCountResponse(1L, "광주", 0L),
                        new StadiumCheckInCountResponse(2L, "잠실", 0L),
                        new StadiumCheckInCountResponse(3L, "고척", 1L),
                        new StadiumCheckInCountResponse(4L, "수원", 0L),
                        new StadiumCheckInCountResponse(5L, "대구", 0L),
                        new StadiumCheckInCountResponse(6L, "부산", 0L),
                        new StadiumCheckInCountResponse(7L, "인천", 0L),
                        new StadiumCheckInCountResponse(8L, "마산", 0L),
                        new StadiumCheckInCountResponse(9L, "대전", 0L)
                )
        );

        // when
        StadiumCheckInCountsResponse actual = checkInService.findStadiumCheckInCounts(member.getId(),
                2025);

        // then
        assertThat(actual).isEqualTo(expected);
    }

    private void createCheckInsForGame(Team team, Game game, int count) {
        for (int i = 0; i < count; i++) {
            Member member = memberFactory.save(b -> b.team(team));
            checkInFactory.save(b -> b.member(member).team(team).game(game));
        }
    }

    private void makeGames(final LocalDate startDate, final List<CheckIn> savedCheckIns, final Member member) {
        Game game1 = gameFactory.save(gameBuilder ->
                gameBuilder.date(startDate.plusDays(0))
                        .stadium(stadiumJamsil)
                        .homeTeam(lotte).homeScore(10).homeScoreBoard(TestFixture.getHomeScoreBoardAbout(10))
                        .awayTeam(kia).awayScore(1).awayScoreBoard(TestFixture.getAwayScoreBoardAbout(1))
                        .homePitcher("일승리")
                        .awayPitcher("일패배")
                        .gameState(GameState.COMPLETED)
        );
        savedCheckIns.add(checkInFactory.save(builder -> builder.team(lotte).member(member).game(game1)));

        Game game2 = gameFactory.save(gameBuilder ->
                gameBuilder.date(startDate.plusDays(1))
                        .stadium(stadiumJamsil)
                        .homeTeam(lotte).homeScore(1).homeScoreBoard(TestFixture.getAwayScoreBoardAbout(1))
                        .awayTeam(kia).awayScore(10).awayScoreBoard(TestFixture.getAwayScoreBoardAbout(10))
                        .homePitcher("이패배")
                        .awayPitcher("이승리")
                        .gameState(GameState.COMPLETED)
        );
        savedCheckIns.add(checkInFactory.save(builder -> builder.team(lotte).member(member).game(game2)));

        Game game3 = gameFactory.save(gameBuilder ->
                gameBuilder.date(startDate.plusDays(2))
                        .stadium(stadiumJamsil)
                        .homeTeam(lotte).homeScore(10).homeScoreBoard(TestFixture.getAwayScoreBoardAbout(10))
                        .awayTeam(kia).awayScore(10).awayScoreBoard(TestFixture.getAwayScoreBoardAbout(10))
                        .homePitcher("삼무승부")
                        .awayPitcher("삼무승부")
                        .gameState(GameState.COMPLETED)
        );
        savedCheckIns.add(checkInFactory.save(builder -> builder.team(lotte).member(member).game(game3)));

        Game game4 = gameFactory.save(gameBuilder ->
                gameBuilder.date(startDate.plusDays(3))
                        .stadium(stadiumJamsil)
                        .homeTeam(lotte).homeScore(10).homeScoreBoard(TestFixture.getAwayScoreBoardAbout(10))
                        .awayTeam(kia).awayScore(1).awayScoreBoard(TestFixture.getAwayScoreBoardAbout(1))
                        .homePitcher("사승리")
                        .awayPitcher("사패배")
                        .gameState(GameState.COMPLETED)
        );
        savedCheckIns.add(checkInFactory.save(builder -> builder.team(lotte).member(member).game(game4)));
    }

    private void makeWinningGames(final LocalDate startDate, final List<CheckIn> savedCheckIns, final Member member) {
        Game game1 = gameFactory.save(b -> b.stadium(stadiumJamsil)
                .homeTeam(kia).homeScore(10)
                .awayTeam(kt).awayScore(1)
                .homeScoreBoard(TestFixture.getHomeScoreBoardAbout(10))
                .awayScoreBoard(TestFixture.getAwayScoreBoardAbout(1))
                .homePitcher("김승리")
                .awayPitcher("최패배")
                .gameState(GameState.COMPLETED)
                .date(startDate));
        savedCheckIns.add(checkInFactory.save(b -> b.member(member).team(member.getTeam()).game(game1)));

        Game game2 = gameFactory.save(b -> b.stadium(stadiumJamsil)
                .homeTeam(kia).homeScore(5)
                .awayTeam(lg).awayScore(4)
                .homeScoreBoard(TestFixture.getHomeScoreBoardAbout(5))
                .awayScoreBoard(TestFixture.getAwayScoreBoardAbout(4))
                .homePitcher("이승리")
                .awayPitcher("송패배")
                .gameState(GameState.COMPLETED)
                .date(startDate.plusDays(1)));
        savedCheckIns.add(checkInFactory.save(b -> b.member(member).team(member.getTeam()).game(game2)));

        Game game3 = gameFactory.save(b -> b.stadium(stadiumJamsil)
                .homeTeam(kia).homeScore(4)
                .awayTeam(samsung).awayScore(0)
                .homeScoreBoard(TestFixture.getHomeScoreBoardAbout(4))
                .awayScoreBoard(TestFixture.getAwayScoreBoardAbout(0))
                .homePitcher("박승리")
                .awayPitcher("공패배")
                .gameState(GameState.COMPLETED)
                .date(startDate.plusDays(2)));
        savedCheckIns.add(checkInFactory.save(b -> b.member(member).team(member.getTeam()).game(game3)));
    }

    private void makeLosingGames(final LocalDate startDate, final List<CheckIn> savedCheckIns, final Member member) {
        Game game1 = gameFactory.save(b -> b.stadium(stadiumJamsil)
                .homeTeam(kt).homeScore(11)
                .awayTeam(kia).awayScore(1)
                .homeScoreBoard(TestFixture.getHomeScoreBoardAbout(11))
                .awayScoreBoard(TestFixture.getAwayScoreBoardAbout(1))
                .homePitcher("포라")
                .awayPitcher("파이브라")
                .gameState(GameState.COMPLETED)
                .date(startDate.plusDays(3)));
        savedCheckIns.add(checkInFactory.save(b -> b.member(member).team(member.getTeam()).game(game1)));

        Game game2 = gameFactory.save(b -> b.stadium(stadiumJamsil)
                .homeTeam(lg).homeScore(5)
                .awayTeam(kia).awayScore(2)
                .homeScoreBoard(TestFixture.getHomeScoreBoardAbout(5))
                .awayScoreBoard(TestFixture.getAwayScoreBoardAbout(2))
                .homePitcher("식스라")
                .awayPitcher("세븐라")
                .gameState(GameState.COMPLETED)
                .date(startDate.plusDays(4)));
        savedCheckIns.add(checkInFactory.save(b -> b.member(member).team(member.getTeam()).game(game2)));

        Game game3 = gameFactory.save(b -> b.stadium(stadiumJamsil)
                .homeTeam(samsung).homeScore(25)
                .awayTeam(kia).awayScore(2)
                .homeScoreBoard(TestFixture.getHomeScoreBoardAbout(25))
                .awayScoreBoard(TestFixture.getAwayScoreBoardAbout(2))
                .homePitcher("에잇라")
                .awayPitcher("나인라")
                .gameState(GameState.COMPLETED)
                .date(startDate.plusDays(5)));
        savedCheckIns.add(checkInFactory.save(b -> b.member(member).team(member.getTeam()).game(game3)));
    }

}
