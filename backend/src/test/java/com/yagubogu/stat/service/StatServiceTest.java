package com.yagubogu.stat.service;

import com.yagubogu.auth.config.AuthTestConfig;
import com.yagubogu.checkin.domain.CheckInType;
import com.yagubogu.checkin.repository.CheckInRepository;
import com.yagubogu.game.domain.Game;
import com.yagubogu.game.domain.GameState;
import com.yagubogu.game.domain.ScoreBoard;
import com.yagubogu.global.config.JpaAuditingConfig;
import com.yagubogu.global.exception.ForbiddenException;
import com.yagubogu.global.exception.NotFoundException;
import com.yagubogu.global.exception.UnprocessableEntityException;
import com.yagubogu.member.domain.Member;
import com.yagubogu.member.domain.Role;
import com.yagubogu.member.repository.MemberRepository;
import com.yagubogu.stadium.domain.Stadium;
import com.yagubogu.stadium.repository.StadiumRepository;
import com.yagubogu.stat.domain.VictoryFairyRanking;
import com.yagubogu.stat.dto.CheckInSummaryParam;
import com.yagubogu.stat.dto.OpponentWinRateTeamParam;
import com.yagubogu.stat.dto.VictoryFairySummaryParam;
import com.yagubogu.stat.dto.v1.AverageStatisticResponse;
import com.yagubogu.stat.dto.v1.LuckyStadiumResponse;
import com.yagubogu.stat.dto.v1.OpponentWinRateResponse;
import com.yagubogu.stat.dto.v1.StatCountsResponse;
import com.yagubogu.stat.dto.v1.WinRateResponse;
import com.yagubogu.stat.repository.VictoryFairyRankingRepository;
import com.yagubogu.support.checkin.CheckInFactory;
import com.yagubogu.support.game.GameFactory;
import com.yagubogu.support.member.MemberBuilder;
import com.yagubogu.support.member.MemberFactory;
import com.yagubogu.team.domain.Team;
import com.yagubogu.team.repository.TeamRepository;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

@Import({AuthTestConfig.class, JpaAuditingConfig.class})
@DataJpaTest
class StatServiceTest {

    private final int RECENT_LIMIT = 10;

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
    private MemberRepository memberRepository;

    @Autowired
    private StadiumRepository stadiumRepository;

    @Autowired
    private VictoryFairyRankingRepository victoryFairyRankingRepository;

    @BeforeEach
    void setUp() {
        statService = new StatService(checkInRepository, memberRepository, victoryFairyRankingRepository);
    }

    @DisplayName("승이 1인 맴버의 통계를 계산한다.")
    @Test
    void findStatCounts_winCounts() {
        // given
        Team HT = teamRepository.findByTeamCode("HT").orElseThrow();
        Team LT = teamRepository.findByTeamCode("LT").orElseThrow();
        Team SS = teamRepository.findByTeamCode("SS").orElseThrow();
        Member member = memberFactory.save(b -> b.team(HT));

        Stadium kia = stadiumRepository.findByShortName("챔피언스필드").orElseThrow();
        Stadium sam = stadiumRepository.findByShortName("라이온즈파크").orElseThrow();
        Stadium lot = stadiumRepository.findByShortName("사직구장").orElseThrow();

        Game g1 = gameFactory.save(b -> b.stadium(kia)
                .homeTeam(HT).awayTeam(LT)
                .date(LocalDate.of(2025, 7, 16))
                .homeScore(10).awayScore(9)
                .gameState(GameState.COMPLETED));

        Game g2 = gameFactory.save(b -> b.stadium(lot)
                .homeTeam(LT).awayTeam(HT)
                .date(LocalDate.of(2025, 7, 17))
                .homeScore(5).awayScore(10)
                .gameState(GameState.COMPLETED));

        Game g3 = gameFactory.save(b -> b.stadium(kia)
                .homeTeam(HT).awayTeam(SS)
                .date(LocalDate.of(2025, 7, 18))
                .homeScore(9).awayScore(4)
                .gameState(GameState.COMPLETED));

        Game g4 = gameFactory.save(b -> b.stadium(sam)
                .homeTeam(SS).awayTeam(HT)
                .date(LocalDate.of(2025, 7, 19))
                .homeScore(3).awayScore(8)
                .gameState(GameState.COMPLETED));

        Game g5 = gameFactory.save(b -> b.stadium(kia)
                .homeTeam(HT).awayTeam(LT)
                .date(LocalDate.of(2025, 7, 20))
                .homeScore(7).awayScore(6)
                .gameState(GameState.COMPLETED));

        Game g6 = gameFactory.save(b -> b.stadium(lot)
                .homeTeam(LT).awayTeam(HT)
                .date(LocalDate.of(2025, 7, 21))
                .homeScore(5).awayScore(5)
                .gameState(GameState.COMPLETED));

        checkInFactory.save(b -> b.game(g1).member(member).team(HT));
        checkInFactory.save(b -> b.game(g2).member(member).team(HT));
        checkInFactory.save(b -> b.game(g3).member(member).team(HT));
        checkInFactory.save(b -> b.game(g4).member(member).team(HT));
        checkInFactory.save(b -> b.game(g5).member(member).team(HT));
        checkInFactory.save(b -> b.game(g6).member(member).team(HT));
        int year = 2025;

        // when
        StatCountsResponse actual = statService.findStatCounts(member.getId(), year);

        // then
        assertSoftly(
                softAssertions -> {
                    softAssertions.assertThat(actual.winCounts()).isEqualTo(5);
                    softAssertions.assertThat(actual.drawCounts()).isEqualTo(1);
                    softAssertions.assertThat(actual.loseCounts()).isEqualTo(0);
                    softAssertions.assertThat(actual.favoriteCheckInCounts()).isEqualTo(6);
                }
        );
    }

    @DisplayName("무가 1인 사용자의 통계를 계산한다")
    @Test
    void findStatCounts_drawCount() {
        // given
        Team LT = teamRepository.findByTeamCode("LT").orElseThrow();
        Team HT = teamRepository.findByTeamCode("HT").orElseThrow();
        Member member = memberFactory.save(b -> b.team(LT));
        Stadium lot = stadiumRepository.findByShortName("사직구장").orElseThrow();

        Game drawGame = gameFactory.save(b -> b.stadium(lot)
                .homeTeam(LT).awayTeam(HT)
                .date(LocalDate.of(2025, 7, 21))
                .homeScore(5).awayScore(5)
                .gameState(GameState.COMPLETED)
        );
        checkInFactory.save(b -> b.game(drawGame).member(member).team(LT));
        int year = 2025;

        // when
        StatCountsResponse actual = statService.findStatCounts(member.getId(), year);

        // then
        assertSoftly(
                softAssertions -> {
                    softAssertions.assertThat(actual.winCounts()).isEqualTo(0);
                    softAssertions.assertThat(actual.drawCounts()).isEqualTo(1);
                    softAssertions.assertThat(actual.loseCounts()).isEqualTo(0);
                    softAssertions.assertThat(actual.favoriteCheckInCounts()).isEqualTo(1);
                }
        );
    }

    @DisplayName("패가 1인 사용자의 통계를 계산한다")
    @Test
    void findStatCounts_loseCount() {
        // given
        Team LT = teamRepository.findByTeamCode("LT").orElseThrow();
        Team HT = teamRepository.findByTeamCode("HT").orElseThrow();
        Member member = memberFactory.save(b -> b.team(LT));
        Stadium lot = stadiumRepository.findByShortName("사직구장").orElseThrow();

        Game drawGame = gameFactory.save(b -> b.stadium(lot)
                .homeTeam(LT).awayTeam(HT)
                .date(LocalDate.of(2025, 7, 21))
                .homeScore(4).awayScore(5)
                .gameState(GameState.COMPLETED)
        );
        checkInFactory.save(b -> b.game(drawGame).member(member).team(LT));
        int year = 2025;

        // when
        StatCountsResponse actual = statService.findStatCounts(member.getId(), year);

        // then
        assertSoftly(
                softAssertions -> {
                    softAssertions.assertThat(actual.winCounts()).isEqualTo(0);
                    softAssertions.assertThat(actual.drawCounts()).isEqualTo(0);
                    softAssertions.assertThat(actual.loseCounts()).isEqualTo(1);
                    softAssertions.assertThat(actual.favoriteCheckInCounts()).isEqualTo(1);
                }
        );
    }

    @DisplayName("예외: 회원이 조회되지 않으면 NotFoundException이 발생한다")
    @Test
    void findStatCounts_notFoundMember() {
        // given
        long memberId = 999L;
        int year = 2025;

        // when & then
        assertThatThrownBy(() -> statService.findStatCounts(memberId, year))
                .isExactlyInstanceOf(NotFoundException.class)
                .hasMessage("Member is not found");
    }

    @DisplayName("예외: 관리자인 경우 ForbiddenException 발생한다")
    @Test
    void findStatCounts_isAdmin() {
        // given
        Member member = memberFactory.save(b -> b.role(Role.ADMIN));
        int year = 2025;

        // when & then
        assertThatThrownBy(() -> statService.findStatCounts(member.getId(), year))
                .isExactlyInstanceOf(ForbiddenException.class)
                .hasMessage("Member should not be admin");
    }

    @DisplayName("승률을 계산한다")
    @Test
    void findWinRate() {
        // given
        Team HT = teamRepository.findByTeamCode("HT").orElseThrow();
        Team LT = teamRepository.findByTeamCode("LT").orElseThrow();
        Team SS = teamRepository.findByTeamCode("SS").orElseThrow();

        Member member = memberFactory.save(b -> b.team(HT));

        Stadium kia = stadiumRepository.findByShortName("챔피언스필드").orElseThrow();
        Stadium lot = stadiumRepository.findByShortName("사직구장").orElseThrow();
        Stadium sam = stadiumRepository.findByShortName("라이온즈파크").orElseThrow();

        // 챔피언스필드: 3승 0패
        Game g1 = gameFactory.save(b -> b.stadium(kia)
                .homeTeam(HT).awayTeam(LT)
                .date(LocalDate.of(2025, 7, 10))
                .homeScore(8).awayScore(5)
                .gameState(GameState.COMPLETED));

        Game g2 = gameFactory.save(b -> b.stadium(kia)
                .homeTeam(HT).awayTeam(SS)
                .date(LocalDate.of(2025, 7, 11))
                .homeScore(7).awayScore(3)
                .gameState(GameState.COMPLETED));

        Game g3 = gameFactory.save(b -> b.stadium(kia)
                .homeTeam(HT).awayTeam(LT)
                .date(LocalDate.of(2025, 7, 12))
                .homeScore(5).awayScore(4)
                .gameState(GameState.COMPLETED));

        // 사직구장: 1승 1패
        Game g4 = gameFactory.save(b -> b.stadium(lot)
                .homeTeam(LT).awayTeam(HT)
                .date(LocalDate.of(2025, 7, 13))
                .homeScore(4).awayScore(6) // 승
                .gameState(GameState.COMPLETED));

        Game g5 = gameFactory.save(b -> b.stadium(lot)
                .homeTeam(LT).awayTeam(HT)
                .date(LocalDate.of(2025, 7, 14))
                .homeScore(7).awayScore(3) // 패
                .gameState(GameState.COMPLETED));

        // 라이온즈파크: 1승 0패
        Game g6 = gameFactory.save(b -> b.stadium(sam)
                .homeTeam(SS).awayTeam(HT)
                .date(LocalDate.of(2025, 7, 15))
                .homeScore(2).awayScore(5) // 승
                .gameState(GameState.COMPLETED));

        // 체크인: 전부 즐겨찾기 팀(HT)으로
        checkInFactory.save(b -> b.game(g1).member(member).team(HT));
        checkInFactory.save(b -> b.game(g2).member(member).team(HT));
        checkInFactory.save(b -> b.game(g3).member(member).team(HT));
        checkInFactory.save(b -> b.game(g4).member(member).team(HT));
        checkInFactory.save(b -> b.game(g5).member(member).team(HT));
        checkInFactory.save(b -> b.game(g6).member(member).team(HT));
        int year = 2025;

        // when
        WinRateResponse actual = statService.findWinRate(member.getId(), year);

        // then
        assertThat(actual.winRate()).isEqualTo(83.3);
    }

    @DisplayName("최근 10경기에 대한 결과만 계산한다: 12경기 중 과거 2경기는 제외되고 최신 10경기의 승/패/무만 카운트된다")
    @Test
    void findRecentTenGamesWinRate_onlyLast10Games() {
        // given
        Team HT = teamRepository.findByTeamCode("HT").orElseThrow();
        Team LT = teamRepository.findByTeamCode("LT").orElseThrow();
        Stadium kia = stadiumRepository.findByShortName("챔피언스필드").orElseThrow();

        Member member = memberFactory.save(b -> b.team(HT));

        for (int d = 1; d <= 20; d++) {
            int day = d;
            int homeScore = 0;
            int awayScore = 0;

            if (day == 8 || day == 10 || day == 11 || day == 13 || day == 15 || day == 17) {
                homeScore = 5;
                awayScore = 2; // 승
            } else if (day == 9 || day == 12 || day == 16) {
                homeScore = 2;
                awayScore = 6; // 패
            } else if (day == 14) {
                homeScore = 4;
                awayScore = 4; // 무
            } else {
                homeScore = 1;
                awayScore = 0; // 그냥 값 (집계대상 아님)
            }

            int hs = homeScore;
            int as = awayScore;

            Game g = gameFactory.save(b -> b.stadium(kia)
                    .homeTeam(HT).awayTeam(LT)
                    .date(LocalDate.of(2025, 8, day))
                    .homeScore(hs).awayScore(as)
                    .gameState(GameState.COMPLETED));

            // 8/05~8/17에만 내가 체크인
            if (day >= 5 && day <= 17) {
                checkInFactory.save(b -> b.game(g).member(member).team(HT));
            }
        }

        // when
        int wins = checkInRepository.findRecentGamesWinCounts(member, 2025, RECENT_LIMIT);
        int loses = checkInRepository.findRecentGamesLoseCounts(member, 2025, RECENT_LIMIT);
        int draws = checkInRepository.findRecentGamesDrawCounts(member, 2025, RECENT_LIMIT);

        // then
        assertSoftly(s -> {
            s.assertThat(wins).isEqualTo(6);
            s.assertThat(loses).isEqualTo(3);
            s.assertThat(draws).isEqualTo(1);
        });
    }

    @DisplayName("0%가 아닌 승률이 있을 때 행운의 구장을 조회한다")
    @Test
    void findLuckyStadium_withWinRate() {
        // given
        Team HT = teamRepository.findByTeamCode("HT").orElseThrow();
        Team LT = teamRepository.findByTeamCode("LT").orElseThrow();

        Member member = memberFactory.save(b -> b.team(HT));
        Stadium kia = stadiumRepository.findByShortName("챔피언스필드").orElseThrow();

        Game g1 = gameFactory.save(b -> b.stadium(kia)
                .homeTeam(HT).awayTeam(LT)
                .date(LocalDate.of(2025, 7, 10))
                .homeScore(6).awayScore(3) // kia 승
                .gameState(GameState.COMPLETED));

        checkInFactory.save(b -> b.game(g1).member(member).team(HT));
        int year = 2025;

        // when
        LuckyStadiumResponse actual = statService.findLuckyStadium(member.getId(), year);

        // then
        assertThat(actual.shortName()).isEqualTo("챔피언스필드");
    }

    @DisplayName("모든 승률이 0%일 때 행운의 구장을 조회한다")
    @Test
    void findLuckyStadium_withOnlyZeroPercentWinRate() {
        // given
        Team HT = teamRepository.findByTeamCode("HT").orElseThrow();
        Team LT = teamRepository.findByTeamCode("LT").orElseThrow();
        Member member = memberFactory.save(b -> b.team(LT));
        Stadium kia = stadiumRepository.findByShortName("챔피언스필드").orElseThrow();
        Game g1 = gameFactory.save(b -> b.stadium(kia)
                .homeTeam(HT).awayTeam(LT)
                .date(LocalDate.of(2025, 7, 10))
                .homeScore(6).awayScore(3)
                .gameState(GameState.COMPLETED));
        checkInFactory.save(b -> b.game(g1).member(member).team(HT));
        int year = 2025;

        // when
        LuckyStadiumResponse actual = statService.findLuckyStadium(member.getId(), year);

        // then
        assertThat(actual.shortName()).isNull();
    }

    @DisplayName("관람횟수가 0일 때 행운의 구장을 조회한다")
    @Test
    void findLuckyStadium_noCheckInCounts() {
        // given
        Team ss = teamRepository.findByTeamCode("SS").orElseThrow();
        Member member = memberFactory.save(builder -> builder.team(ss));
        int year = 2025;

        // when
        LuckyStadiumResponse actual = statService.findLuckyStadium(member.getId(), year);

        // then
        assertThat(actual.shortName()).isNull();
    }

    @DisplayName("평균 득, 실, 실책, 안타, 피안타를 조회한다")
    @Test
    void findAverageStatistic() {
        // given
        Team HT = teamRepository.findByTeamCode("HT").orElseThrow();
        Team LT = teamRepository.findByTeamCode("LT").orElseThrow();

        Member member = memberFactory.save(b -> b.team(HT));
        Stadium kia = stadiumRepository.findByShortName("챔피언스필드").orElseThrow();

        Game g1 = gameFactory.save(b -> b.stadium(kia)
                .homeTeam(HT).awayTeam(LT)
                .date(LocalDate.of(2025, 7, 10))
                .homeScore(8).awayScore(5)
                .homeScoreBoard(new ScoreBoard(8, 12, 0, 0,
                        List.of("0", "1", "2", "0", "0", "2", "0", "0", "0", "-", "-", "-")))
                .awayScoreBoard(new ScoreBoard(5, 9, 1, 0,
                        List.of("0", "1", "2", "0", "0", "2", "0", "0", "0", "-", "-", "-")))
                .gameState(GameState.COMPLETED));

        Game g2 = gameFactory.save(b -> b.stadium(kia)
                .homeTeam(LT).awayTeam(HT)
                .date(LocalDate.of(2025, 7, 11))
                .homeScore(4).awayScore(10)
                .homeScoreBoard(new ScoreBoard(4, 8, 0, 0,
                        List.of("0", "1", "2", "0", "0", "2", "0", "0", "0", "-", "-", "-")))
                .awayScoreBoard(new ScoreBoard(10, 13, 0, 0,
                        List.of("0", "1", "2", "0", "0", "2", "0", "0", "0", "-", "-", "-")))
                .gameState(GameState.COMPLETED));

        Game g3 = gameFactory.save(b -> b.stadium(kia)
                .homeTeam(HT).awayTeam(LT)
                .date(LocalDate.of(2025, 7, 12))
                .homeScore(5).awayScore(7)
                .homeScoreBoard(new ScoreBoard(5, 11, 1, 0,
                        List.of("0", "1", "2", "0", "0", "2", "0", "0", "0", "-", "-", "-")))
                .awayScoreBoard(new ScoreBoard(7, 10, 0, 0,
                        List.of("0", "1", "2", "0", "0", "2", "0", "0", "0", "-", "-", "-")))
                .gameState(GameState.COMPLETED));

        checkInFactory.save(b -> b.game(g1).member(member).team(HT));
        checkInFactory.save(b -> b.game(g2).member(member).team(HT));
        checkInFactory.save(b -> b.game(g3).member(member).team(HT));

        // when
        AverageStatisticResponse actual = statService.findAverageStatistic(member.getId());

        // then
        assertSoftly(softly -> {
            softly.assertThat(actual.averageRun()).isEqualTo(7.7);
            softly.assertThat(actual.concededRuns()).isEqualTo(5.3);
            softly.assertThat(actual.averageErrors()).isEqualTo(0.3);
            softly.assertThat(actual.averageHits()).isEqualTo(12.0);
            softly.assertThat(actual.concededHits()).isEqualTo(9.0);
        });
    }

    @DisplayName("평균 득, 실, 실책, 안타, 피안타를 조회 시 해당되는게 하나도 없으면 null을 반환한다")
    @Test
    void findAverageStatistic_nullCheck() {
        // given
        Team HT = teamRepository.findByTeamCode("HT").orElseThrow();
        Member member = memberFactory.save(b -> b.team(HT));

        // when
        AverageStatisticResponse actual = statService.findAverageStatistic(member.getId());

        // then
        assertThat(actual)
                .satisfies(response -> {
                    assertThat(response.averageRun()).isNull();
                    assertThat(response.concededRuns()).isNull();
                    assertThat(response.averageErrors()).isNull();
                    assertThat(response.averageHits()).isNull();
                    assertThat(response.concededHits()).isNull();
                });
    }

    @DisplayName("상대팀별 승률을 계산하여 승률 내림차순, 이름 오름차순으로 정렬해 반환하며 미대결 팀은 0.0으로 포함한다")
    @Test
    void findOpponentWinRate_sorted() {
        // given
        Team HT = teamRepository.findByTeamCode("HT").orElseThrow();
        Team LT = teamRepository.findByTeamCode("LT").orElseThrow();
        Team SS = teamRepository.findByTeamCode("SS").orElseThrow();
        Team NC = teamRepository.findByTeamCode("NC").orElseThrow();

        Member member = memberFactory.save(b -> b.team(HT));

        Stadium kia = stadiumRepository.findByShortName("챔피언스필드").orElseThrow();
        Stadium sam = stadiumRepository.findByShortName("라이온즈파크").orElseThrow();
        Stadium lot = stadiumRepository.findByShortName("사직구장").orElseThrow();

        // 2025-07: HT vs SS → 2승
        Game g1 = gameFactory.save(b -> b.stadium(kia)
                .homeTeam(HT).awayTeam(SS)
                .date(LocalDate.of(2025, 7, 10))
                .homeScore(5).awayScore(3)
                .gameState(GameState.COMPLETED));
        checkInFactory.save(b -> b.member(member).team(HT).game(g1));

        Game g2 = gameFactory.save(b -> b.stadium(sam)
                .homeTeam(SS).awayTeam(HT)
                .date(LocalDate.of(2025, 7, 11))
                .homeScore(2).awayScore(4)
                .gameState(GameState.COMPLETED));
        checkInFactory.save(b -> b.member(member).team(HT).game(g2));

        // 2025-07: HT vs LT → 1승 1패
        Game g3 = gameFactory.save(b -> b.stadium(kia)
                .homeTeam(HT).awayTeam(LT)
                .date(LocalDate.of(2025, 7, 12))
                .homeScore(6).awayScore(2)
                .gameState(GameState.COMPLETED));
        checkInFactory.save(b -> b.member(member).team(HT).game(g3));

        Game g4 = gameFactory.save(b -> b.stadium(lot)
                .homeTeam(LT).awayTeam(HT)
                .date(LocalDate.of(2025, 7, 13))
                .homeScore(7).awayScore(1)
                .gameState(GameState.COMPLETED));
        checkInFactory.save(b -> b.member(member).team(HT).game(g4));

        // 2025-07: HT vs NC → 1무
        Game g5 = gameFactory.save(b -> b.stadium(kia)
                .homeTeam(HT).awayTeam(NC)
                .date(LocalDate.of(2025, 7, 14))
                .homeScore(4).awayScore(4)
                .gameState(GameState.COMPLETED));
        checkInFactory.save(b -> b.member(member).team(HT).game(g5));

        int year = 2025;

        // when
        OpponentWinRateResponse actual = statService.findOpponentWinRate(member.getId(), year);

        // then
        // 내 팀(HT) 제외한 상대 팀 수를 동적으로 계산 (레거시/과거팀 포함 대응)
        int expectedOpponents = 9;

        assertSoftly(s -> {
            // 1) 사이즈 검증(고정 9 → 동적 계산)
            s.assertThat(actual.opponents()).hasSize(expectedOpponents);

            // 2) 상단 랭킹 고정값 검증
            OpponentWinRateTeamParam first = actual.opponents().get(0);
            s.assertThat(first.teamCode()).isEqualTo("SS");
            s.assertThat(first.wins()).isEqualTo(2);
            s.assertThat(first.losses()).isEqualTo(0);
            s.assertThat(first.draws()).isEqualTo(0);
            s.assertThat(first.winRate()).isEqualTo(100.0);

            OpponentWinRateTeamParam second = actual.opponents().get(1);
            s.assertThat(second.teamCode()).isEqualTo("LT");
            s.assertThat(second.wins()).isEqualTo(1);
            s.assertThat(second.losses()).isEqualTo(1);
            s.assertThat(second.draws()).isEqualTo(0);
            s.assertThat(second.winRate()).isEqualTo(50.0);

            // 3) NC는 1무로 승률 0.0
            OpponentWinRateTeamParam ncRes = actual.opponents().stream()
                    .filter(r -> r.teamCode().equals("NC"))
                    .findFirst().orElseThrow();
            s.assertThat(ncRes.wins()).isZero();
            s.assertThat(ncRes.losses()).isZero();
            s.assertThat(ncRes.draws()).isEqualTo(1);
            s.assertThat(ncRes.winRate()).isEqualTo(0.0);

            // 4) 미대결(또는 무만 있는) 팀: winRate == 0.0
            Set<String> zeroCodesActual = actual.opponents().stream()
                    .filter(r -> r.winRate() == 0.0)
                    .map(OpponentWinRateTeamParam::teamCode)
                    .collect(Collectors.toSet());

            // 기대 집합 = 전체 팀코드 - {내 팀 HT, SS, LT}  (SS/LT는 100/50이라 제외)
            Set<String> zeroCodesExpected = Set.of("HH", "OB", "NC", "SK", "WO", "KT", "LG");

            s.assertThat(zeroCodesActual).isEqualTo(zeroCodesExpected);

            // 5) 전체 정렬 규칙 검증: 승률 desc → 이름 asc
            List<OpponentWinRateTeamParam> sorted = actual.opponents().stream()
                    .sorted(Comparator
                            .comparing(OpponentWinRateTeamParam::winRate).reversed()
                            .thenComparing(OpponentWinRateTeamParam::name))
                    .toList();
            s.assertThat(actual.opponents()).containsExactlyElementsOf(sorted);
        });
    }

    @DisplayName("연도 범위를 벗어난 경기는 집계에서 제외되며 미대결 팀은 0.0으로 포함된다")
    @Test
    void findOpponentWinRate_excludes_other_year_and_includes_unplayed() {
        // given
        Team HT = teamRepository.findByTeamCode("HT").orElseThrow();
        Team LT = teamRepository.findByTeamCode("LT").orElseThrow();
        Member member = memberFactory.save(b -> b.team(HT));
        Stadium kia = stadiumRepository.findByShortName("챔피언스필드").orElseThrow();

        // 2024(제외)
        Game game2024 = gameFactory.save(b -> b.stadium(kia)
                .homeTeam(HT).awayTeam(LT)
                .date(LocalDate.of(2024, 9, 1))
                .homeScore(10).awayScore(1)
                .gameState(GameState.COMPLETED));
        checkInFactory.save(b -> b.member(member).team(HT).game(game2024));

        // 2025(포함, 승)
        Game game2025 = gameFactory.save(b -> b.stadium(kia)
                .homeTeam(HT).awayTeam(LT)
                .date(LocalDate.of(2025, 9, 1))
                .homeScore(3).awayScore(2)
                .gameState(GameState.COMPLETED));
        checkInFactory.save(b -> b.member(member).team(HT).game(game2025));

        // when
        OpponentWinRateResponse actual = statService.findOpponentWinRate(member.getId(), 2025);

        // then
        assertSoftly(s -> {
            s.assertThat(actual.opponents()).hasSize(9);
            OpponentWinRateTeamParam lt = actual.opponents().stream()
                    .filter(it -> it.teamCode().equals("LT"))
                    .findFirst().orElseThrow();
            s.assertThat(lt.winRate()).isEqualTo(100.0);
            s.assertThat(lt.wins()).isEqualTo(1);
            s.assertThat(lt.losses()).isEqualTo(0);
            s.assertThat(lt.draws()).isEqualTo(0);

            s.assertThat(actual.opponents().stream()
                    .filter(it -> !it.teamCode().equals("LT"))
                    .allMatch(it -> it.winRate() == 0.0)).isTrue();
        });
    }

    @DisplayName("예외: 회원에 팀이 없으면 NotFoundException을 던진다")
    @Test
    void findOpponentWinRate_member_without_team() {
        // given
        Member member = memberFactory.save(MemberBuilder::build);

        // when & then
        assertThatThrownBy(() -> statService.findOpponentWinRate(member.getId(), 2025))
                .isExactlyInstanceOf(UnprocessableEntityException.class)
                .hasMessage("Team should not be null");
    }

    @DisplayName("점수 미기록 경기는 제외되며 그 외 미대결 팀은 0.0으로 포함된다")
    @Test
    void findOpponentWinRate_ignores_null_scores_and_includes_unplayed() {
        // given
        Team HT = teamRepository.findByTeamCode("HT").orElseThrow();
        Team LT = teamRepository.findByTeamCode("LT").orElseThrow();
        Member member = memberFactory.save(b -> b.team(HT));
        Stadium kia = stadiumRepository.findByShortName("챔피언스필드").orElseThrow();

        // 미기록(제외: SCHEDULED)
        Game gScheduled = gameFactory.save(b -> b.stadium(kia)
                .homeTeam(HT).awayTeam(LT)
                .date(LocalDate.of(2025, 7, 30))
                .homeScore(null).awayScore(null)
                .gameState(GameState.SCHEDULED));
        checkInFactory.save(b -> b.member(member).team(HT).game(gScheduled));

        // 기록(포함, 승)
        Game gCompleted = gameFactory.save(b -> b.stadium(kia)
                .homeTeam(HT).awayTeam(LT)
                .date(LocalDate.of(2025, 8, 2))
                .homeScore(2).awayScore(1)
                .gameState(GameState.COMPLETED));
        checkInFactory.save(b -> b.member(member).team(HT).game(gCompleted));

        // when
        OpponentWinRateResponse actual = statService.findOpponentWinRate(member.getId(), 2025);

        // then
        assertSoftly(s -> {
            s.assertThat(actual.opponents()).hasSize(9);
            OpponentWinRateTeamParam lt = actual.opponents().stream()
                    .filter(it -> it.teamCode().equals("LT"))
                    .findFirst().orElseThrow();
            s.assertThat(lt.winRate()).isEqualTo(100.0);
            s.assertThat(lt.wins()).isEqualTo(1);
            s.assertThat(lt.losses()).isEqualTo(0);
            s.assertThat(lt.draws()).isEqualTo(0);

            s.assertThat(actual.opponents().stream()
                    .filter(it -> !it.teamCode().equals("LT"))
                    .allMatch(it -> it.winRate() == 0.0)).isTrue();
        });
    }

    @DisplayName("game만 존재하고 해당 회원의 checkIn이 없으면 그 경기는 승률 집계에서 제외된다")
    @Test
    void findOpponentWinRate_excludes_games_without_member_checkin() {
        // given
        Team HT = teamRepository.findByTeamCode("HT").orElseThrow();
        Team LT = teamRepository.findByTeamCode("LT").orElseThrow();
        Stadium kia = stadiumRepository.findByShortName("챔피언스필드").orElseThrow();
        Stadium lot = stadiumRepository.findByShortName("사직구장").orElseThrow();

        // 집계 대상 회원(응원팀 HT)
        Member member = memberFactory.save(b -> b.team(HT));

        // 다른 회원(같은 팀 HT) — 이 회원의 체크인은 대상 회원 집계에 포함되면 안 됨
        Member other = memberFactory.save(b -> b.team(HT));

        // 2025-08-01: HT(home) vs LT — COMPLETED (대상 회원 체크인 없음 → 제외)
        Game gHome = gameFactory.save(b -> b.stadium(kia)
                .homeTeam(HT).awayTeam(LT)
                .date(LocalDate.of(2025, 8, 1))
                .homeScore(3).awayScore(2)
                .gameState(GameState.COMPLETED));
        checkInFactory.save(b -> b.member(other).team(HT).game(gHome));

        // 2025-08-02: LT(home) vs HT — COMPLETED (대상 회원 체크인 없음 → 제외)
        Game gAway = gameFactory.save(b -> b.stadium(lot)
                .homeTeam(LT).awayTeam(HT)
                .date(LocalDate.of(2025, 8, 2))
                .homeScore(1).awayScore(5)
                .gameState(GameState.COMPLETED));
        checkInFactory.save(b -> b.member(other).team(HT).game(gAway));

        // when
        OpponentWinRateResponse actual = statService.findOpponentWinRate(member.getId(), 2025);

        // then
        int expectedOpponents = 9;

        assertSoftly(s -> {
            s.assertThat(actual.opponents()).hasSize(expectedOpponents);

            // LT도 대상 회원 체크인이 없으므로 0.0이어야 함
            OpponentWinRateTeamParam lt = actual.opponents().stream()
                    .filter(it -> it.teamCode().equals("LT"))
                    .findFirst().orElseThrow();
            s.assertThat(lt.wins()).isEqualTo(0);
            s.assertThat(lt.losses()).isEqualTo(0);
            s.assertThat(lt.draws()).isEqualTo(0);
            s.assertThat(lt.winRate()).isEqualTo(0.0);

            // 모든 상대팀이 0.0 (집계된 경기가 없음)
            s.assertThat(actual.opponents().stream()
                            .allMatch(it -> it.winRate() == 0.0))
                    .isTrue();
        });
    }

    @DisplayName("PastCheckIn과 CheckIn을 통합하여 승패무 통계를 계산한다")
    @Test
    void findStatCounts_withPastCheckIn() {
        // given
        Team HT = teamRepository.findByTeamCode("HT").orElseThrow();
        Team LT = teamRepository.findByTeamCode("LT").orElseThrow();
        Member member = memberFactory.save(b -> b.team(HT));
        Stadium kia = stadiumRepository.findByShortName("챔피언스필드").orElseThrow();

        // CheckIn: 2승 1무
        Game g1 = gameFactory.save(b -> b.stadium(kia)
                .homeTeam(HT).awayTeam(LT)
                .date(LocalDate.of(2025, 7, 1))
                .homeScore(5).awayScore(3)
                .gameState(GameState.COMPLETED));
        checkInFactory.save(b -> b.game(g1).member(member).team(HT));

        Game g2 = gameFactory.save(b -> b.stadium(kia)
                .homeTeam(HT).awayTeam(LT)
                .date(LocalDate.of(2025, 7, 2))
                .homeScore(4).awayScore(4)
                .gameState(GameState.COMPLETED));
        checkInFactory.save(b -> b.game(g2).member(member).team(HT));

        Game g3 = gameFactory.save(b -> b.stadium(kia)
                .homeTeam(LT).awayTeam(HT)
                .date(LocalDate.of(2025, 7, 3))
                .homeScore(2).awayScore(6)
                .gameState(GameState.COMPLETED));
        checkInFactory.save(b -> b.game(g3).member(member).team(HT));

        // PastCheckIn: 1승 1패
        Game g4 = gameFactory.save(b -> b.stadium(kia)
                .homeTeam(HT).awayTeam(LT)
                .date(LocalDate.of(2025, 6, 4))
                .homeScore(7).awayScore(5)
                .gameState(GameState.COMPLETED));
        checkInFactory.save(b -> b.game(g4).member(member).team(HT).checkInType(CheckInType.NON_LOCATION_CHECK_IN));

        Game g5 = gameFactory.save(b -> b.stadium(kia)
                .homeTeam(LT).awayTeam(HT)
                .date(LocalDate.of(2025, 6, 5))
                .homeScore(8).awayScore(3)
                .gameState(GameState.COMPLETED));
        checkInFactory.save(b -> b.game(g5).member(member).team(HT).checkInType(CheckInType.NON_LOCATION_CHECK_IN));

        int year = 2025;

        // when
        StatCountsResponse actual = statService.findStatCounts(member.getId(), year);

        // then: CheckIn(2승 0패 1무) + PastCheckIn(1승 1패 0무) = 3승 1패 1무, 총 5경기
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(actual.winCounts()).isEqualTo(3);
            softAssertions.assertThat(actual.drawCounts()).isEqualTo(1);
            softAssertions.assertThat(actual.loseCounts()).isEqualTo(1);
            softAssertions.assertThat(actual.favoriteCheckInCounts()).isEqualTo(5);
        });
    }

    @DisplayName("PastCheckIn과 CheckIn을 통합하여 승률을 계산한다")
    @Test
    void findWinRate_withPastCheckIn() {
        // given
        Team HT = teamRepository.findByTeamCode("HT").orElseThrow();
        Team LT = teamRepository.findByTeamCode("LT").orElseThrow();
        Member member = memberFactory.save(b -> b.team(HT));
        Stadium kia = stadiumRepository.findByShortName("챔피언스필드").orElseThrow();

        // CheckIn: 2승 1패
        Game g1 = gameFactory.save(b -> b.stadium(kia)
                .homeTeam(HT).awayTeam(LT)
                .date(LocalDate.of(2025, 7, 1))
                .homeScore(5).awayScore(3)
                .gameState(GameState.COMPLETED));
        checkInFactory.save(b -> b.game(g1).member(member).team(HT));

        Game g2 = gameFactory.save(b -> b.stadium(kia)
                .homeTeam(LT).awayTeam(HT)
                .date(LocalDate.of(2025, 7, 2))
                .homeScore(6).awayScore(3)
                .gameState(GameState.COMPLETED));
        checkInFactory.save(b -> b.game(g2).member(member).team(HT));

        Game g3 = gameFactory.save(b -> b.stadium(kia)
                .homeTeam(HT).awayTeam(LT)
                .date(LocalDate.of(2025, 7, 3))
                .homeScore(7).awayScore(2)
                .gameState(GameState.COMPLETED));
        checkInFactory.save(b -> b.game(g3).member(member).team(HT));

        // PastCheckIn: 1승 1패
        Game g4 = gameFactory.save(b -> b.stadium(kia)
                .homeTeam(HT).awayTeam(LT)
                .date(LocalDate.of(2025, 6, 4))
                .homeScore(8).awayScore(5)
                .gameState(GameState.COMPLETED));
        checkInFactory.save(b -> b.game(g4).member(member).team(HT).checkInType(CheckInType.NON_LOCATION_CHECK_IN));

        Game g5 = gameFactory.save(b -> b.stadium(kia)
                .homeTeam(LT).awayTeam(HT)
                .date(LocalDate.of(2025, 6, 5))
                .homeScore(9).awayScore(4)
                .gameState(GameState.COMPLETED));
        checkInFactory.save(b -> b.game(g5).member(member).team(HT).checkInType(CheckInType.NON_LOCATION_CHECK_IN));

        int year = 2025;

        // when
        WinRateResponse actual = statService.findWinRate(member.getId(), year);

        // then: 총 3승 2패 = 60.0%
        assertThat(actual.winRate()).isEqualTo(60.0);
    }

    @DisplayName("PastCheckIn과 CheckIn을 통합하여 행운의 구장을 조회한다")
    @Test
    void findLuckyStadium_withPastCheckIn() {
        // given
        Team HT = teamRepository.findByTeamCode("HT").orElseThrow();
        Team LT = teamRepository.findByTeamCode("LT").orElseThrow();
        Member member = memberFactory.save(b -> b.team(HT));

        Stadium kia = stadiumRepository.findByShortName("챔피언스필드").orElseThrow();
        Stadium lot = stadiumRepository.findByShortName("사직구장").orElseThrow();

        // CheckIn: 챔피언스필드 1승
        Game g1 = gameFactory.save(b -> b.stadium(kia)
                .homeTeam(HT).awayTeam(LT)
                .date(LocalDate.of(2025, 7, 1))
                .homeScore(5).awayScore(3)
                .gameState(GameState.COMPLETED));
        checkInFactory.save(b -> b.game(g1).member(member).team(HT));

        // PastCheckIn: 챔피언스필드 1승, 사직구장 1승 1패
        Game g2 = gameFactory.save(b -> b.stadium(kia)
                .homeTeam(HT).awayTeam(LT)
                .date(LocalDate.of(2025, 6, 2))
                .homeScore(6).awayScore(4)
                .gameState(GameState.COMPLETED));
        checkInFactory.save(b -> b.game(g2).member(member).team(HT).checkInType(CheckInType.NON_LOCATION_CHECK_IN));

        Game g3 = gameFactory.save(b -> b.stadium(lot)
                .homeTeam(LT).awayTeam(HT)
                .date(LocalDate.of(2025, 6, 3))
                .homeScore(2).awayScore(5)
                .gameState(GameState.COMPLETED));
        checkInFactory.save(b -> b.game(g3).member(member).team(HT).checkInType(CheckInType.NON_LOCATION_CHECK_IN));

        Game g4 = gameFactory.save(b -> b.stadium(lot)
                .homeTeam(HT).awayTeam(LT)
                .date(LocalDate.of(2025, 6, 4))
                .homeScore(3).awayScore(7)
                .gameState(GameState.COMPLETED));
        checkInFactory.save(b -> b.game(g4).member(member).team(HT).checkInType(CheckInType.NON_LOCATION_CHECK_IN));

        int year = 2025;

        // when
        LuckyStadiumResponse actual = statService.findLuckyStadium(member.getId(), year);

        // then: 챔피언스필드(2승 0패 = 100%) > 사직구장(1승 1패 = 50%)
        assertThat(actual.shortName()).isEqualTo("챔피언스필드");
    }

    @DisplayName("PastCheckIn과 CheckIn을 통합하여 평균 통계를 조회한다")
    @Test
    void findAverageStatistic_withPastCheckIn() {
        // given
        Team HT = teamRepository.findByTeamCode("HT").orElseThrow();
        Team LT = teamRepository.findByTeamCode("LT").orElseThrow();
        Member member = memberFactory.save(b -> b.team(HT));
        Stadium kia = stadiumRepository.findByShortName("챔피언스필드").orElseThrow();

        // CheckIn: 1경기
        Game g1 = gameFactory.save(b -> b.stadium(kia)
                .homeTeam(HT).awayTeam(LT)
                .date(LocalDate.of(2025, 7, 1))
                .homeScore(6).awayScore(4)
                .homeScoreBoard(new ScoreBoard(6, 10, 1, 0,
                        List.of("0", "1", "2", "0", "0", "2", "0", "0", "0", "-", "-", "-")))
                .awayScoreBoard(new ScoreBoard(4, 8, 0, 0,
                        List.of("0", "1", "2", "0", "0", "2", "0", "0", "0", "-", "-", "-")))
                .gameState(GameState.COMPLETED));
        checkInFactory.save(b -> b.game(g1).member(member).team(HT));

        // PastCheckIn: 1경기
        Game g2 = gameFactory.save(b -> b.stadium(kia)
                .homeTeam(LT).awayTeam(HT)
                .date(LocalDate.of(2025, 6, 2))
                .homeScore(5).awayScore(8)
                .homeScoreBoard(new ScoreBoard(5, 9, 0, 0,
                        List.of("0", "1", "2", "0", "0", "2", "0", "0", "0", "-", "-", "-")))
                .awayScoreBoard(new ScoreBoard(8, 12, 1, 0,
                        List.of("0", "1", "2", "0", "0", "2", "0", "0", "0", "-", "-", "-")))
                .gameState(GameState.COMPLETED));
        checkInFactory.save(b -> b.game(g2).member(member).team(HT).checkInType(CheckInType.NON_LOCATION_CHECK_IN));

        // when
        AverageStatisticResponse actual = statService.findAverageStatistic(member.getId());

        // then: 평균 득점 = (6+8)/2 = 7.0, 평균 실점 = (4+5)/2 = 4.5, 평균 실책 = (1+1)/2 = 1.0, 평균 안타 = (10+12)/2 = 11.0, 평균 피안타 = (8+9)/2 = 8.5
        assertSoftly(softly -> {
            softly.assertThat(actual.averageRun()).isEqualTo(7.0);
            softly.assertThat(actual.concededRuns()).isEqualTo(4.5);
            softly.assertThat(actual.averageErrors()).isEqualTo(1.0);
            softly.assertThat(actual.averageHits()).isEqualTo(11.0);
            softly.assertThat(actual.concededHits()).isEqualTo(8.5);
        });
    }

    @DisplayName("PastCheckIn과 CheckIn을 통합하여 상대팀별 승률을 조회한다")
    @Test
    void findOpponentWinRate_withPastCheckIn() {
        // given
        Team HT = teamRepository.findByTeamCode("HT").orElseThrow();
        Team LT = teamRepository.findByTeamCode("LT").orElseThrow();
        Team SS = teamRepository.findByTeamCode("SS").orElseThrow();
        Member member = memberFactory.save(b -> b.team(HT));
        Stadium kia = stadiumRepository.findByShortName("챔피언스필드").orElseThrow();

        // CheckIn: LT와 1승
        Game g1 = gameFactory.save(b -> b.stadium(kia)
                .homeTeam(HT).awayTeam(LT)
                .date(LocalDate.of(2025, 7, 1))
                .homeScore(5).awayScore(3)
                .gameState(GameState.COMPLETED));
        checkInFactory.save(b -> b.game(g1).member(member).team(HT));

        // PastCheckIn: LT와 1패, SS와 1승
        Game g2 = gameFactory.save(b -> b.stadium(kia)
                .homeTeam(LT).awayTeam(HT)
                .date(LocalDate.of(2025, 6, 2))
                .homeScore(6).awayScore(4)
                .gameState(GameState.COMPLETED));
        checkInFactory.save(b -> b.game(g2).member(member).team(HT).checkInType(CheckInType.NON_LOCATION_CHECK_IN));

        Game g3 = gameFactory.save(b -> b.stadium(kia)
                .homeTeam(HT).awayTeam(SS)
                .date(LocalDate.of(2025, 6, 3))
                .homeScore(7).awayScore(2)
                .gameState(GameState.COMPLETED));
        checkInFactory.save(b -> b.game(g3).member(member).team(HT).checkInType(CheckInType.NON_LOCATION_CHECK_IN));

        int year = 2025;

        // when
        OpponentWinRateResponse actual = statService.findOpponentWinRate(member.getId(), year);

        // then
        assertSoftly(s -> {
            s.assertThat(actual.opponents()).hasSize(9);

            // SS: 1승 0패 = 100%
            OpponentWinRateTeamParam ss = actual.opponents().stream()
                    .filter(it -> it.teamCode().equals("SS"))
                    .findFirst().orElseThrow();
            s.assertThat(ss.wins()).isEqualTo(1);
            s.assertThat(ss.losses()).isEqualTo(0);
            s.assertThat(ss.winRate()).isEqualTo(100.0);

            // LT: 1승 1패 = 50%
            OpponentWinRateTeamParam lt = actual.opponents().stream()
                    .filter(it -> it.teamCode().equals("LT"))
                    .findFirst().orElseThrow();
            s.assertThat(lt.wins()).isEqualTo(1);
            s.assertThat(lt.losses()).isEqualTo(1);
            s.assertThat(lt.winRate()).isEqualTo(50.0);
        });
    }

    @DisplayName("사용자의 체크인 요약 정보(승/무/패, 승률, 최근 체크인 날짜)를 정상적으로 조회한다")
    @Test
    void findCheckInSummary_success() {
        // given
        Team HT = teamRepository.findByTeamCode("HT").orElseThrow();
        Team LT = teamRepository.findByTeamCode("LT").orElseThrow();
        Member member = memberFactory.save(b -> b.team(HT).nickname("우가").build());
        Stadium kia = stadiumRepository.findByShortName("챔피언스필드").orElseThrow();
        int year = 2025;

        // 2승 1무 1패 데이터 생성
        Game winGame1 = gameFactory.save(b -> b.stadium(kia)
                .homeTeam(HT).awayTeam(LT)
                .date(LocalDate.of(year, 8, 1))
                .homeScore(5).awayScore(1)
                .gameState(GameState.COMPLETED));
        checkInFactory.save(b -> b.member(member).team(HT).game(winGame1));

        Game loseGame = gameFactory.save(b -> b.stadium(kia)
                .homeTeam(HT).awayTeam(LT)
                .date(LocalDate.of(year, 8, 2))
                .homeScore(2).awayScore(3)
                .gameState(GameState.COMPLETED));
        checkInFactory.save(b -> b.member(member).team(HT).game(loseGame));

        Game drawGame = gameFactory.save(b -> b.stadium(kia)
                .homeTeam(HT).awayTeam(LT)
                .date(LocalDate.of(year, 8, 3))
                .homeScore(4).awayScore(4)
                .gameState(GameState.COMPLETED));
        checkInFactory.save(b -> b.member(member).team(HT).game(drawGame));

        // 이 경기가 가장 최신 체크인이 됨
        Game winGame2 = gameFactory.save(b -> b.stadium(kia)
                .homeTeam(HT).awayTeam(LT)
                .date(LocalDate.of(year, 8, 5))
                .homeScore(7).awayScore(0)
                .gameState(GameState.COMPLETED));
        checkInFactory.save(b -> b.member(member).team(HT).game(winGame2));

        // when
        CheckInSummaryParam actual = statService.findCheckInSummary(member.getId(), year);

        // then
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(actual.winCounts()).isEqualTo(2);
            softAssertions.assertThat(actual.drawCounts()).isEqualTo(1);
            softAssertions.assertThat(actual.loseCounts()).isEqualTo(1);
            softAssertions.assertThat(actual.totalCount()).isEqualTo(4);
            softAssertions.assertThat(actual.winRate()).isEqualTo(66.7);
            softAssertions.assertThat(actual.recentCheckInDate()).isEqualTo(LocalDate.of(year, 8, 5));
        });
    }

    @DisplayName("체크인 기록이 없는 사용자의 요약 정보를 조회하면 모든 값이 0또는 null로 반환된다")
    @Test
    void findCheckInSummary_noCheckIns() {
        // given
        Member member = memberFactory.save(b -> b.team(teamRepository.findByTeamCode("HT").orElseThrow())
                .nickname("우가").build());
        int year = 2025;

        // when
        CheckInSummaryParam actual = statService.findCheckInSummary(member.getId(), year);

        // then
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(actual.winCounts()).isZero();
            softAssertions.assertThat(actual.drawCounts()).isZero();
            softAssertions.assertThat(actual.loseCounts()).isZero();
            softAssertions.assertThat(actual.totalCount()).isZero();
            softAssertions.assertThat(actual.winRate()).isEqualTo(0.0);
            softAssertions.assertThat(actual.recentCheckInDate()).isNull();
        });
    }

    @DisplayName("승/패 기록 없이 무승부만 있는 경우 승률은 0.0%가 된다")
    @Test
    void findCheckInSummary_onlyDraws() {
        // given
        Team HT = teamRepository.findByTeamCode("HT").orElseThrow();
        Team LT = teamRepository.findByTeamCode("LT").orElseThrow();
        Member member = memberFactory.save(b -> b.team(HT).nickname("우가").build());
        Stadium kia = stadiumRepository.findByShortName("챔피언스필드").orElseThrow();
        int year = 2025;

        Game drawGame = gameFactory.save(b -> b.stadium(kia)
                .homeTeam(HT).awayTeam(LT)
                .date(LocalDate.of(year, 8, 3))
                .homeScore(4).awayScore(4)
                .gameState(GameState.COMPLETED));
        checkInFactory.save(b -> b.member(member).team(HT).game(drawGame));

        // when
        CheckInSummaryParam actual = statService.findCheckInSummary(member.getId(), year);

        // then
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(actual.winCounts()).isZero();
            softAssertions.assertThat(actual.drawCounts()).isEqualTo(1);
            softAssertions.assertThat(actual.loseCounts()).isZero();
            softAssertions.assertThat(actual.totalCount()).isEqualTo(1);
            softAssertions.assertThat(actual.winRate()).isEqualTo(0.0);
            softAssertions.assertThat(actual.recentCheckInDate()).isEqualTo(LocalDate.of(year, 8, 3));
        });
    }

    @DisplayName("다른 연도의 체크인 기록은 요약 정보 집계에서 제외된다")
    @Test
    void findCheckInSummary_filtersByYear() {
        // given
        Team HT = teamRepository.findByTeamCode("HT").orElseThrow();
        Team LT = teamRepository.findByTeamCode("LT").orElseThrow();
        Member member = memberFactory.save(b -> b.team(HT).nickname("우가").build());
        Stadium kia = stadiumRepository.findByShortName("챔피언스필드").orElseThrow();
        int targetYear = 2025;
        int nonTargetYear = 2024;

        // 2024년 기록 (제외 대상)
        Game game2024 = gameFactory.save(b -> b.stadium(kia)
                .homeTeam(HT).awayTeam(LT)
                .date(LocalDate.of(nonTargetYear, 10, 1))
                .homeScore(10).awayScore(1) // 승
                .gameState(GameState.COMPLETED));
        checkInFactory.save(b -> b.member(member).team(HT).game(game2024));

        // 2025년 기록 (포함 대상)
        Game game2025 = gameFactory.save(b -> b.stadium(kia)
                .homeTeam(HT).awayTeam(LT)
                .date(LocalDate.of(targetYear, 8, 2))
                .homeScore(2).awayScore(3) // 패
                .gameState(GameState.COMPLETED));
        checkInFactory.save(b -> b.member(member).team(HT).game(game2025));

        // when
        CheckInSummaryParam actual = statService.findCheckInSummary(member.getId(), targetYear);

        // then
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(actual.winCounts()).isZero(); // 2025년 승은 없음
            softAssertions.assertThat(actual.drawCounts()).isZero();
            softAssertions.assertThat(actual.loseCounts()).isEqualTo(1);
            softAssertions.assertThat(actual.totalCount()).isEqualTo(1);
            softAssertions.assertThat(actual.winRate()).isEqualTo(0.0);
            softAssertions.assertThat(actual.recentCheckInDate()).isEqualTo(LocalDate.of(targetYear, 8, 2));
        });
    }

    @DisplayName("당일 경기(SCHEDULED)는 집계에서 제외하고, 과거 경기(COMPLETED)만 집계한다")
    @Test
    void findCheckInSummary_excludesScheduledFutureGames() {
        // given
        Team HT = teamRepository.findByTeamCode("HT").orElseThrow();
        Team LT = teamRepository.findByTeamCode("LT").orElseThrow();
        Member member = memberFactory.save(b -> b.team(HT));
        Stadium kia = stadiumRepository.findByShortName("챔피언스필드").orElseThrow();
        LocalDate today = LocalDate.now();

        // 1. 과거 경기 (COMPLETED, 승) - 집계 포함 대상
        Game pastCompletedGame = gameFactory.save(b -> b.stadium(kia)
                .homeTeam(HT).awayTeam(LT)
                .date(today.minusDays(1)) // 어제 경기
                .homeScore(5).awayScore(1)
                .gameState(GameState.COMPLETED));
        checkInFactory.save(b -> b.member(member).team(HT).game(pastCompletedGame));

        // 2. 당일 경기 (SCHEDULED) - 집계 제외 대상
        Game todayScheduledGame = gameFactory.save(b -> b.stadium(kia)
                .homeTeam(HT).awayTeam(LT)
                .date(today) // 오늘 경기
                .homeScore(null).awayScore(null)
                .gameState(GameState.SCHEDULED));
        checkInFactory.save(b -> b.member(member).team(HT).game(todayScheduledGame));

        // when
        CheckInSummaryParam actual = statService.findCheckInSummary(member.getId(), today.getYear());

        // then
        assertSoftly(s -> {
            s.assertThat(actual.winCounts()).isEqualTo(1);
            s.assertThat(actual.drawCounts()).isZero();
            s.assertThat(actual.loseCounts()).isZero();
            s.assertThat(actual.totalCount()).isEqualTo(1);
            s.assertThat(actual.winRate()).isEqualTo(100.0);
            s.assertThat(actual.recentCheckInDate()).isEqualTo(today.minusDays(1));
        });
    }

    @DisplayName("COMPLETED 경기만 통계에 집계하고, CANCELED, SCHEDULED 경기는 제외한다")
    @Test
    void findCheckInSummary_filtersByGameState() {
        // given
        Team HT = teamRepository.findByTeamCode("HT").orElseThrow();
        Team LT = teamRepository.findByTeamCode("LT").orElseThrow();
        Member member = memberFactory.save(b -> b.team(HT));
        Stadium kia = stadiumRepository.findByShortName("챔피언스필드").orElseThrow();
        int year = 2025;

        // 1. COMPLETED (승)
        Game completedGame = gameFactory.save(b -> b.stadium(kia)
                .homeTeam(HT).awayTeam(LT)
                .date(LocalDate.of(year, 8, 1))
                .homeScore(5).awayScore(1)
                .gameState(GameState.COMPLETED));
        checkInFactory.save(b -> b.member(member).team(HT).game(completedGame));

        // 2. CANCELED
        Game canceledGame = gameFactory.save(b -> b.stadium(kia)
                .homeTeam(HT).awayTeam(LT)
                .date(LocalDate.of(year, 8, 2))
                .homeScore(null).awayScore(null)
                .gameState(GameState.CANCELED));
        checkInFactory.save(b -> b.member(member).team(HT).game(canceledGame));

        // 3. SCHEDULED
        Game scheduledGame = gameFactory.save(b -> b.stadium(kia)
                .homeTeam(HT).awayTeam(LT)
                .date(LocalDate.of(year, 8, 3))
                .homeScore(null).awayScore(null)
                .gameState(GameState.SCHEDULED));
        checkInFactory.save(b -> b.member(member).team(HT).game(scheduledGame));

        // when
        CheckInSummaryParam actual = statService.findCheckInSummary(member.getId(), year);

        // then
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(actual.winCounts()).isEqualTo(1);
            softAssertions.assertThat(actual.drawCounts()).isZero();
            softAssertions.assertThat(actual.loseCounts()).isZero();
            softAssertions.assertThat(actual.totalCount()).isEqualTo(1);
            softAssertions.assertThat(actual.winRate()).isEqualTo(100.0);
            softAssertions.assertThat(actual.recentCheckInDate()).isEqualTo(LocalDate.of(year, 8, 2));
        });
    }

    @DisplayName("승리요정 전체 순위와 팀 내 순위가 모두 있을 때 요약 정보를 정상적으로 조회한다")
    @Test
    void findVictoryFairySummary_success() {
        // given
        Team HT = teamRepository.findByTeamCode("HT").orElseThrow();
        Team LT = teamRepository.findByTeamCode("LT").orElseThrow();
        int year = 2025;

        Member targetMember = memberFactory.save(b -> b.team(HT).nickname("우가"));
        Member rivalMember = memberFactory.save(b -> b.team(HT).nickname("두리"));
        Member otherTeamMember = memberFactory.save(b -> b.team(LT).nickname("밍트"));

        // 1등: otherTeamMember (200.0)
        // 2등: rivalMember (100.0)
        // 3등: targetMember (50.0)
        victoryFairyRankingRepository.save(new VictoryFairyRanking(otherTeamMember, 200.0, 20, 30, year, null));
        victoryFairyRankingRepository.save(new VictoryFairyRanking(rivalMember, 100.0, 10, 20, year, null));
        victoryFairyRankingRepository.save(new VictoryFairyRanking(targetMember, 50.0, 5, 10, year, null));

        // when
        VictoryFairySummaryParam actual = statService.findVictoryFairySummary(targetMember.getId(), year);

        // then
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(actual.ranking()).isEqualTo(3);
            softAssertions.assertThat(actual.score()).isEqualTo(50.0);
            softAssertions.assertThat(actual.rankWithinTeam()).isEqualTo(2);
        });
    }

    @DisplayName("승리요정 랭킹 정보가 전혀 없을 때, 모든 필드가 null로 반환된다")
    @Test
    void findVictoryFairySummary_noRankingData() {
        // given
        Member member = memberFactory.save(b -> b.team(teamRepository.findByTeamCode("HT").orElseThrow()));
        int year = 2025;

        // when
        VictoryFairySummaryParam actual = statService.findVictoryFairySummary(member.getId(), year);

        // then
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(actual.ranking()).isNull();
            softAssertions.assertThat(actual.score()).isNull();
            softAssertions.assertThat(actual.rankWithinTeam()).isNull();
        });
    }

    @DisplayName("팀 내 동점자가 있을 경우 다음 순위가 올바르게 계산된다")
    @Test
    void findVictoryFairySummary_handlesTies() {
        // given
        Team HT = teamRepository.findByTeamCode("HT").orElseThrow();
        int year = 2025;

        // 공동 1위 멤버 2명과, 그 다음 순위인 대상 멤버 1명 생성
        Member targetMember = memberFactory.save(b -> b.team(HT).nickname("우가"));
        Member jointFirst1 = memberFactory.save(b -> b.team(HT).nickname("메다"));
        Member jointFirst2 = memberFactory.save(b -> b.team(HT).nickname("포라"));

        // 랭킹 데이터 저장: 2명이 100점으로 공동 1위, 대상 멤버는 50점
        victoryFairyRankingRepository.save(new VictoryFairyRanking(jointFirst1, 100.0, 10, 10, year, null));
        victoryFairyRankingRepository.save(new VictoryFairyRanking(jointFirst2, 100.0, 10, 10, year, null));
        victoryFairyRankingRepository.save(new VictoryFairyRanking(targetMember, 50.0, 5, 5, year, null));

        // when
        VictoryFairySummaryParam actual = statService.findVictoryFairySummary(targetMember.getId(), year);

        // then
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(actual.ranking()).isEqualTo(3);
            softAssertions.assertThat(actual.score()).isEqualTo(50.0);
            softAssertions.assertThat(actual.rankWithinTeam()).isEqualTo(3);
        });
    }

    @DisplayName("다른 연도의 랭킹 데이터는 현재 연도 조회 시 영향을 주지 않는다")
    @Test
    void findVictoryFairySummary_isolatesByYear() {
        // given
        Team HT = teamRepository.findByTeamCode("HT").orElseThrow();
        int targetYear = 2025;
        int otherYear = 2024;

        Member targetMember = memberFactory.save(b -> b.team(HT).nickname("우가"));
        Member otherYearWinner = memberFactory.save(b -> b.team(HT).nickname("포르"));

        // 2025년: targetMember가 100점으로 1위
        victoryFairyRankingRepository.save(new VictoryFairyRanking(targetMember, 100.0, 10, 10, targetYear, null));

        // 2024년: 다른 멤버가 훨씬 높은 점수를 가짐 (이 데이터는 무시되어야 함)
        victoryFairyRankingRepository.save(new VictoryFairyRanking(otherYearWinner, 999.0, 100, 100, otherYear, null));
        victoryFairyRankingRepository.save(new VictoryFairyRanking(targetMember, 50.0, 5, 5, otherYear, null));

        // when
        VictoryFairySummaryParam actual = statService.findVictoryFairySummary(targetMember.getId(), targetYear);

        // then
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(actual.ranking()).isEqualTo(1);
            softAssertions.assertThat(actual.score()).isEqualTo(100.0);
            softAssertions.assertThat(actual.rankWithinTeam()).isEqualTo(1);
        });
    }
}
