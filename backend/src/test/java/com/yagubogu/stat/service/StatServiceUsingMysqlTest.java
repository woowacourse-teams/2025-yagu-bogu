package com.yagubogu.stat.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.yagubogu.auth.config.AuthTestConfig;
import com.yagubogu.checkin.dto.TeamFilter;
import com.yagubogu.checkin.dto.VictoryFairyRankingResponses;
import com.yagubogu.checkin.dto.VictoryFairyRankingResponses.VictoryFairyRankingResponse;
import com.yagubogu.checkin.repository.CheckInRepository;
import com.yagubogu.game.domain.Game;
import com.yagubogu.game.domain.GameState;
import com.yagubogu.global.config.JpaAuditingConfig;
import com.yagubogu.member.domain.Member;
import com.yagubogu.stadium.domain.Stadium;
import com.yagubogu.stadium.repository.StadiumRepository;
import com.yagubogu.stat.domain.VictoryFairyRanking;
import com.yagubogu.stat.repository.VictoryFairyRankingRepository;
import com.yagubogu.support.TestFixture;
import com.yagubogu.support.base.ServiceUsingMysqlTestBase;
import com.yagubogu.support.checkin.CheckInFactory;
import com.yagubogu.support.game.GameFactory;
import com.yagubogu.support.member.MemberFactory;
import com.yagubogu.team.domain.Team;
import com.yagubogu.team.repository.TeamRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@Import({AuthTestConfig.class, JpaAuditingConfig.class})
class StatServiceUsingMysqlTest extends ServiceUsingMysqlTestBase {

    @Autowired
    private StatService statService;

    @Autowired
    private CheckInRepository checkInRepository;

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
    private VictoryFairyRankingRepository victoryFairyRankingRepository;

    private Team kia, kt, lg, samsung, doosan, lotte;
    private Stadium stadiumJamsil, stadiumGocheok, stadiumIncheon;


    @BeforeEach
    void setUp() {
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

    @DisplayName("승/패 회원 모두 랭킹이 갱신된다")
    @Test
    void calculateVictoryScore() {
        // given
        Team HT = teamRepository.findByTeamCode("HT").orElseThrow();
        Team LT = teamRepository.findByTeamCode("LT").orElseThrow();

        Member member1 = memberFactory.save(b -> b.team(HT));
        Member member2 = memberFactory.save(b -> b.team(HT));
        Member member3 = memberFactory.save(b -> b.team(LT));

        Stadium stadium = stadiumRepository.findByShortName("챔피언스필드").orElseThrow();
        Game game1 = gameFactory.save(b -> b.stadium(stadium)
                .homeTeam(HT).awayTeam(LT)
                .date(LocalDate.of(2025, 8, 1))
                .homeScore(5).awayScore(3)
                .gameState(GameState.COMPLETED)); // HT 승
        Game game2 = gameFactory.save(b -> b.stadium(stadium)
                .homeTeam(HT).awayTeam(LT)
                .date(LocalDate.of(2025, 9, 1))
                .homeScore(5).awayScore(3)
                .gameState(GameState.COMPLETED)); // HT 승
        Game lastYearGame = gameFactory.save(b -> b.stadium(stadium)
                .homeTeam(HT).awayTeam(LT)
                .date(LocalDate.of(2024, 9, 1))
                .homeScore(5).awayScore(8)
                .gameState(GameState.COMPLETED)); // LG 승

        // 각 회원이 자신의 응원팀으로 체크인
        checkInFactory.save(b -> b.game(game1).member(member1).team(HT));  // 승
        checkInFactory.save(b -> b.game(game2).member(member1).team(HT));  // 승
        checkInFactory.save(b -> b.game(game2).member(member2).team(HT));  // 승
        checkInFactory.save(b -> b.game(game2).member(member3).team(LT)); // 패
        checkInFactory.save(b -> b.game(lastYearGame).member(member3).team(LT)); // 승 -> 작년 경기이므로 영향 없음

        int lastYear = 2024;
        int year = 2025;

        // m, c 계산 (서비스 내부와 동일한 레포지토리 계산 사용)
        double m = checkInRepository.calculateTotalAverageWinRate(year);
        double c = checkInRepository.calculateAverageCheckInCount(year);

        // when
        statService.calculateVictoryFairyScore(year, game1.getId());
        statService.calculateVictoryFairyScore(year, game2.getId());
        statService.calculateVictoryFairyScore(lastYear, lastYearGame.getId());

        // then
        List<VictoryFairyRanking> rankings = victoryFairyRankingRepository.findAll();
        VictoryFairyRanking rWin1 = rankings.stream()
                .filter(v -> v.getMember().getId().equals(member1.getId()) && v.getGameYear() == year)
                .findFirst().orElseThrow();
        VictoryFairyRanking rWin2 = rankings.stream()
                .filter(v -> v.getMember().getId().equals(member2.getId()) && v.getGameYear() == year)
                .findFirst().orElseThrow();
        VictoryFairyRanking rLose1 = rankings.stream()
                .filter(v -> v.getMember().getId().equals(member3.getId()) && v.getGameYear() == year).findFirst()
                .orElseThrow();

        double expectedMember1Score = getScore((2 + c * m) / (2 + c));
        double expectedMember2Score = getScore((1 + c * m) / (1 + c));
        double expectedMember3Score = getScore((0 + c * m) / (1 + c));

        assertSoftly(s -> {
            // 승리 회원: winCount=1, checkInCount=1, 점수는 승리 공식
            s.assertThat(rWin1.getWinCount()).isEqualTo(2);
            s.assertThat(rWin1.getCheckInCount()).isEqualTo(2);
            s.assertThat(rWin1.getScore()).isEqualTo(expectedMember1Score);

            s.assertThat(rWin2.getWinCount()).isEqualTo(1);
            s.assertThat(rWin2.getCheckInCount()).isEqualTo(1);
            s.assertThat(rWin2.getScore()).isEqualTo(expectedMember2Score);

            // 패배 회원: winCount=0, checkInCount=1, 점수는 패배 공식
            s.assertThat(rLose1.getWinCount()).isEqualTo(0);
            s.assertThat(rLose1.getCheckInCount()).isEqualTo(1);
            s.assertThat(rLose1.getScore()).isEqualTo(expectedMember3Score);

            // 승리 점수가 패배 점수보다 커야 한다
            s.assertThat(rWin1.getScore()).isGreaterThan(rLose1.getScore());
        });
    }

    @DisplayName("승리 요정 랭킹 조회 중 회원이 인증한 정보가 없는 경우에 null이 아닌 회원 정보가 반환된다")
    @Test
    void findVictoryFairyRankings_notCheckInForMember() {
        // given
        Member fora = memberFactory.save(b -> b.team(kia).nickname("포라"));
        long memberId = fora.getId();

        // when
        VictoryFairyRankingResponses actual = statService.findVictoryFairyRankings(memberId, TeamFilter.ALL, 2025);

        // then
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(actual.myRanking().ranking()).isEqualTo(0);
            softAssertions.assertThat(actual.myRanking().nickname()).isEqualTo(fora.getNickname().getValue());
            softAssertions.assertThat(actual.myRanking().teamShortName()).isEqualTo(fora.getTeam().getShortName());
            softAssertions.assertThat(actual.myRanking().victoryFairyScore()).isEqualTo(0.0);
        });
    }

    @DisplayName("회원이 응원하지 않는 팀의 경기를 관람하는 경우에 대한 승리 요정 랭킹을 조회한다")
    @Test
    void findVictoryFairyRankings_noFavoriteCheckIn() {
        // given
        Member por = memberFactory.save(b -> b.team(samsung).nickname("포르"));

        LocalDate startDate = LocalDate.of(2025, 7, 21);
        Game game1 = gameFactory.save(b -> b.stadium(stadiumJamsil)
                .homeTeam(kia)
                .awayTeam(kt)
                .homeScoreBoard(TestFixture.getHomeScoreBoardAbout(10))
                .awayScoreBoard(TestFixture.getAwayScoreBoardAbout(1))
                .homeScore(10)
                .awayScore(1)
                .date(startDate)
                .gameState(GameState.COMPLETED)
        );
        Game game2 = gameFactory.save(b -> b.stadium(stadiumJamsil)
                .homeTeam(samsung)
                .awayTeam(kt)
                .homeScoreBoard(TestFixture.getHomeScoreBoardAbout(10))
                .awayScoreBoard(TestFixture.getAwayScoreBoardAbout(1))
                .homeScore(10)
                .awayScore(1)
                .date(startDate)
                .gameState(GameState.COMPLETED)
        );

        checkInFactory.save(builder -> builder
                .team(samsung)
                .member(por)
                .game(game1)
        );
        checkInFactory.save(builder -> builder.team(samsung)
                .member(por)
                .game(game2)
        );

        statService.calculateVictoryFairyScore(startDate.getYear(), game1.getId());
        statService.calculateVictoryFairyScore(startDate.getYear(), game2.getId());

        // when
        VictoryFairyRankingResponses actual = statService.findVictoryFairyRankings(por.getId(), TeamFilter.ALL,
                2025);

        // then
        assertSoftly(softAssertions -> {
                    softAssertions.assertThat(actual.topRankings())
                            .extracting("nickname")
                            .containsExactly("포르");
                    softAssertions.assertThat(actual.myRanking().nickname()).isEqualTo("포르");
                    softAssertions.assertThat(actual.myRanking().teamShortName()).isEqualTo("삼성");
                    softAssertions.assertThat(actual.myRanking().victoryFairyScore()).isEqualTo(66.67);
                }
        );
    }

    @DisplayName("승리 요정 랭킹을 조회한다. 베이즈 정리로 정렬되어 반환된다")
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
                .homeScoreBoard(TestFixture.getHomeScoreBoardAbout(0))
                .awayScoreBoard(TestFixture.getAwayScoreBoardAbout(0))
                .date(startDate.plusDays(6))
                .gameState(GameState.COMPLETED)
        );
        Game scheduledGame = gameFactory.save(b -> b.stadium(stadiumJamsil)
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
        checkInFavorite(duri, g1, g4, g7); // LG는 위 셋에서 모두 패배

        // 삼성 팬: 우가(3경기 중 0승 → 0%)
        checkInFavorite(uga, g3, g5, g6, scheduledGame);

        statService.calculateVictoryFairyScore(startDate.getYear(), g1.getId());
        statService.calculateVictoryFairyScore(startDate.getYear(), g2.getId());
        statService.calculateVictoryFairyScore(startDate.getYear(), g3.getId());
        statService.calculateVictoryFairyScore(startDate.getYear(), g4.getId());
        statService.calculateVictoryFairyScore(startDate.getYear(), g5.getId());
        statService.calculateVictoryFairyScore(startDate.getYear(), g6.getId());
        statService.calculateVictoryFairyScore(startDate.getYear(), g7.getId());
        statService.calculateVictoryFairyScore(startDate.getYear(), scheduledGame.getId());

        // when
        VictoryFairyRankingResponses actual = statService.findVictoryFairyRankings(duri.getId(), TeamFilter.ALL,
                2025);

        // then
        assertSoftly(softAssertions -> {
                    softAssertions.assertThat(actual.topRankings())
                            .extracting("nickname")
                            .containsExactly("밍트", "포르", "포라", "두리", "우가");
                    softAssertions.assertThat(actual.myRanking().nickname()).isEqualTo(duri.getNickname().getValue());
                    softAssertions.assertThat(actual.myRanking().teamShortName()).isEqualTo(duri.getTeam().getShortName());
                    softAssertions.assertThat(actual.myRanking().victoryFairyScore()).isEqualTo(43.75);
                    softAssertions.assertThat(actual.myRanking().ranking()).isEqualTo(4);
                }
        );
    }

    @DisplayName("승리 요정 랭킹을 팀별로 조회한다")
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
        Game g3 = gameFactory.save(b -> b.stadium(stadiumJamsil)
                .homeTeam(kt).homeScore(10)
                .awayTeam(lg).awayScore(1)
                .homeScoreBoard(TestFixture.getHomeScoreBoardAbout(10))
                .awayScoreBoard(TestFixture.getAwayScoreBoardAbout(1))
                .date(startDate.plusDays(3))
                .gameState(GameState.COMPLETED)
        );

        checkInFavorite(mint, g1);
        checkInFavorite(por, g1, g2);
        checkInFavorite(duri, g1, g3);

        statService.calculateVictoryFairyScore(startDate.getYear(), g1.getId());
        statService.calculateVictoryFairyScore(startDate.getYear(), g2.getId());
        statService.calculateVictoryFairyScore(startDate.getYear(), g3.getId());

        // when
        VictoryFairyRankingResponses actual = statService.findVictoryFairyRankings(por.getId(), TeamFilter.HT, 2025);

        // then
        assertSoftly(softAssertions -> {
                    softAssertions.assertThat(actual.topRankings())
                            .extracting("nickname")
                            .containsExactly("포르", "밍트");
                    softAssertions.assertThat(actual.myRanking().nickname()).isEqualTo("포르");
                    softAssertions.assertThat(actual.myRanking().teamShortName()).isEqualTo("KIA");
                    softAssertions.assertThat(actual.myRanking().victoryFairyScore()).isEqualTo(90.0);
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

        statService.calculateVictoryFairyScore(startDate.getYear(), g1.getId());
        statService.calculateVictoryFairyScore(startDate.getYear(), g2.getId());

        // when
        VictoryFairyRankingResponse myRanking = statService.findVictoryFairyRankings(mint.getId(),
                TeamFilter.ALL,
                startDate.getYear()).myRanking();

        // then
        assertThat(myRanking.victoryFairyScore()).isEqualTo(66.67);
    }

    @DisplayName("승리 요정 랭킹 조회 - 인증을 한 번도 하지않은 회원의 순위는 0위이다")
    @Test
    void findVictoryFairyRankings_withoutCheckIn() {
        // given
        Member fora = memberFactory.save(b -> b.team(kia).nickname("포라"));
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

        statService.calculateVictoryFairyScore(startDate.getYear(), g1.getId());
        statService.calculateVictoryFairyScore(startDate.getYear(), g2.getId());

        // when
        VictoryFairyRankingResponses responses = statService.findVictoryFairyRankings(fora.getId(),
                TeamFilter.ALL,
                startDate.getYear());
        List<VictoryFairyRankingResponse> victoryFairyRankingResponses = responses.topRankings();
        VictoryFairyRankingResponse myRanking = responses.myRanking();

        // then
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(victoryFairyRankingResponses).hasSize(1);
            softAssertions.assertThat(myRanking.victoryFairyScore()).isEqualTo(0.0);
            softAssertions.assertThat(myRanking.victoryFairyScore()).isEqualTo(0);
        });
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

    private double getScore(double value) {
        return BigDecimal.valueOf(value)
                .multiply(BigDecimal.valueOf(100)) // 100.0 *
                .setScale(2, RoundingMode.HALF_UP) // ROUND(..., 2)
                .doubleValue();
    }
}
