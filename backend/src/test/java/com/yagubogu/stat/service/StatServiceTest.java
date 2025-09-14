package com.yagubogu.stat.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.yagubogu.auth.config.AuthTestConfig;
import com.yagubogu.checkin.repository.CheckInRepository;
import com.yagubogu.game.domain.Game;
import com.yagubogu.game.domain.GameState;
import com.yagubogu.game.domain.ScoreBoard;
import com.yagubogu.global.config.JpaAuditingConfig;
import com.yagubogu.global.exception.ForbiddenException;
import com.yagubogu.global.exception.NotFoundException;
import com.yagubogu.member.domain.Member;
import com.yagubogu.member.domain.Role;
import com.yagubogu.member.repository.MemberRepository;
import com.yagubogu.stadium.domain.Stadium;
import com.yagubogu.stadium.repository.StadiumRepository;
import com.yagubogu.stat.dto.AverageStatisticResponse;
import com.yagubogu.stat.dto.LuckyStadiumResponse;
import com.yagubogu.stat.dto.OpponentWinRateResponse;
import com.yagubogu.stat.dto.OpponentWinRateTeamResponse;
import com.yagubogu.stat.dto.StatCountsResponse;
import com.yagubogu.stat.dto.WinRateResponse;
import com.yagubogu.support.checkin.CheckInFactory;
import com.yagubogu.support.game.GameFactory;
import com.yagubogu.support.member.MemberBuilder;
import com.yagubogu.support.member.MemberFactory;
import com.yagubogu.team.domain.Team;
import com.yagubogu.team.repository.TeamRepository;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@Import({AuthTestConfig.class, JpaAuditingConfig.class})
@DataJpaTest
class StatServiceTest {

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

    @BeforeEach
    void setUp() {
        statService = new StatService(checkInRepository, memberRepository, stadiumRepository, teamRepository);
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
        assertSoftly(s -> {
            s.assertThat(actual.opponents()).hasSize(9);

            // SS
            s.assertThat(actual.opponents().get(0).teamCode()).isEqualTo("SS");
            s.assertThat(actual.opponents().get(0).wins()).isEqualTo(2);
            s.assertThat(actual.opponents().get(0).losses()).isEqualTo(0);
            s.assertThat(actual.opponents().get(0).draws()).isEqualTo(0);
            s.assertThat(actual.opponents().get(0).winRate()).isEqualTo(100.0);

            // LT
            s.assertThat(actual.opponents().get(1).teamCode()).isEqualTo("LT");
            s.assertThat(actual.opponents().get(1).wins()).isEqualTo(1);
            s.assertThat(actual.opponents().get(1).losses()).isEqualTo(1);
            s.assertThat(actual.opponents().get(1).draws()).isEqualTo(0);
            s.assertThat(actual.opponents().get(1).winRate()).isEqualTo(50.0);

            // NC
            OpponentWinRateTeamResponse nc = actual.opponents().stream()
                    .filter(r -> r.teamCode().equals("NC"))
                    .findFirst().orElseThrow();
            s.assertThat(nc.wins()).isEqualTo(0);
            s.assertThat(nc.losses()).isEqualTo(0);
            s.assertThat(nc.draws()).isEqualTo(1);
            s.assertThat(nc.winRate()).isEqualTo(0.0);

            // 미대결 팀들
            var zeroTeamCodes = actual.opponents().stream()
                    .filter(r -> r.winRate() == 0.0)
                    .map(OpponentWinRateTeamResponse::teamCode)
                    .toList();
            s.assertThat(zeroTeamCodes)
                    .containsExactlyInAnyOrder("KT", "LG", "NC", "SK", "OB", "WO", "HH");
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
            OpponentWinRateTeamResponse lt = actual.opponents().stream()
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
                .isExactlyInstanceOf(NotFoundException.class)
                .hasMessage("Team not exist");
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
        var gScheduled = gameFactory.save(b -> b.stadium(kia)
                .homeTeam(HT).awayTeam(LT)
                .date(LocalDate.of(2025, 7, 30))
                .homeScore(null).awayScore(null)
                .gameState(GameState.SCHEDULED));
        checkInFactory.save(b -> b.member(member).team(HT).game(gScheduled));

        // 기록(포함, 승)
        var gCompleted = gameFactory.save(b -> b.stadium(kia)
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
            OpponentWinRateTeamResponse lt = actual.opponents().stream()
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

        // 2025-08-01: HT(home) vs LT — COMPLETED
        var gHome = gameFactory.save(b -> b.stadium(kia)
                .homeTeam(HT).awayTeam(LT)
                .date(LocalDate.of(2025, 8, 1))
                .homeScore(3).awayScore(2)
                .gameState(GameState.COMPLETED));
        // 대상 회원의 CheckIn 없음
        checkInFactory.save(b -> b.member(other).team(HT).game(gHome));

        // 2025-08-02: LT(home) vs HT — COMPLETED
        var gAway = gameFactory.save(b -> b.stadium(lot)
                .homeTeam(LT).awayTeam(HT)
                .date(LocalDate.of(2025, 8, 2))
                .homeScore(1).awayScore(5)
                .gameState(GameState.COMPLETED));
        // 대상 회원의 CheckIn 없음
        checkInFactory.save(b -> b.member(other).team(HT).game(gAway));

        // when
        OpponentWinRateResponse actual = statService.findOpponentWinRate(member.getId(), 2025);

        // then
        assertSoftly(s -> {
            // 전체 10개 팀 중 내 팀(HT) 제외 → 9개
            s.assertThat(actual.opponents()).hasSize(9);

            // LT는 두 경기가 있었지만, 대상 회원의 CheckIn이 없어 집계 제외되어 0.0이어야 한다.
            OpponentWinRateTeamResponse lt = actual.opponents().stream()
                    .filter(it -> it.teamCode().equals("LT"))
                    .findFirst().orElseThrow();

            s.assertThat(lt.wins()).isEqualTo(0);
            s.assertThat(lt.losses()).isEqualTo(0);
            s.assertThat(lt.draws()).isEqualTo(0);
            s.assertThat(lt.winRate()).isEqualTo(0.0);

            // 나머지 미대결팀들도 0.0인지 확인 (LT 포함)
            s.assertThat(actual.opponents().stream()
                    .allMatch(it -> it.winRate() == 0.0)).isTrue();
        });
    }
}
