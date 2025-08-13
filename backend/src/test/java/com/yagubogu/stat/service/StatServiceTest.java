package com.yagubogu.stat.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.yagubogu.auth.config.AuthTestConfig;
import com.yagubogu.checkin.repository.CheckInRepository;
import com.yagubogu.game.domain.Game;
import com.yagubogu.game.domain.GameState;
import com.yagubogu.game.domain.ScoreBoard;
import com.yagubogu.global.exception.ForbiddenException;
import com.yagubogu.global.exception.NotFoundException;
import com.yagubogu.member.domain.Member;
import com.yagubogu.member.domain.Role;
import com.yagubogu.member.repository.MemberRepository;
import com.yagubogu.stadium.domain.Stadium;
import com.yagubogu.stadium.repository.StadiumRepository;
import com.yagubogu.stat.dto.AverageStatisticResponse;
import com.yagubogu.stat.dto.LuckyStadiumResponse;
import com.yagubogu.stat.dto.StatCountsResponse;
import com.yagubogu.stat.dto.WinRateResponse;
import com.yagubogu.support.checkin.CheckInFactory;
import com.yagubogu.support.game.GameFactory;
import com.yagubogu.support.member.MemberBuilder;
import com.yagubogu.support.member.MemberFactory;
import com.yagubogu.team.domain.Team;
import com.yagubogu.team.repository.TeamRepository;
import java.time.LocalDate;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = {
        "spring.sql.init.data-locations=classpath:test-data.sql"
})
@Import(AuthTestConfig.class)
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
        statService = new StatService(checkInRepository, memberRepository, stadiumRepository);
    }

    @DisplayName("승이 1인 맴버의 통계를 계산한다.")
    @Test
    void findStatCounts_winCounts() {
        // given
        Team HT = teamRepository.findByTeamCode("HT").orElseThrow();
        Team LT = teamRepository.findByTeamCode("LT").orElseThrow();
        Team SS = teamRepository.findByTeamCode("SS").orElseThrow();
        Member member = memberFactory.save(b -> b.team(HT));

        Stadium KIA = stadiumRepository.findById(5L).orElseThrow();
        Stadium SAM = stadiumRepository.findById(6L).orElseThrow();
        Stadium LOT = stadiumRepository.findById(9L).orElseThrow();

        Game g1 = gameFactory.save(b -> b.stadium(KIA)
                .homeTeam(HT).awayTeam(LT)
                .date(LocalDate.of(2025, 7, 16))
                .homeScore(10).awayScore(9)
                .gameState(GameState.COMPLETED));

        Game g2 = gameFactory.save(b -> b.stadium(LOT)
                .homeTeam(LT).awayTeam(HT)
                .date(LocalDate.of(2025, 7, 17))
                .homeScore(5).awayScore(10)
                .gameState(GameState.COMPLETED));

        Game g3 = gameFactory.save(b -> b.stadium(KIA)
                .homeTeam(HT).awayTeam(SS)
                .date(LocalDate.of(2025, 7, 18))
                .homeScore(9).awayScore(4)
                .gameState(GameState.COMPLETED));

        Game g4 = gameFactory.save(b -> b.stadium(SAM)
                .homeTeam(SS).awayTeam(HT)
                .date(LocalDate.of(2025, 7, 19))
                .homeScore(3).awayScore(8)
                .gameState(GameState.COMPLETED));

        Game g5 = gameFactory.save(b -> b.stadium(KIA)
                .homeTeam(HT).awayTeam(LT)
                .date(LocalDate.of(2025, 7, 20))
                .homeScore(7).awayScore(6)
                .gameState(GameState.COMPLETED));

        Game g6 = gameFactory.save(b -> b.stadium(LOT)
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
        Stadium LOT = stadiumRepository.findById(9L).orElseThrow();

        Game drawGame = gameFactory.save(b -> b.stadium(LOT)
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
        Stadium LOT = stadiumRepository.findById(9L).orElseThrow();

        Game drawGame = gameFactory.save(b -> b.stadium(LOT)
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

    @DisplayName("회원이 조회되지 않으면 NotFoundException이 발생한다")
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

    @DisplayName("관리자인 경우 ForbiddenException 발생한다")
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

        Stadium KIA = stadiumRepository.findById(5L).orElseThrow();
        Stadium LOT = stadiumRepository.findById(9L).orElseThrow();
        Stadium SAM = stadiumRepository.findById(6L).orElseThrow();

        // 챔피언스필드: 3승 0패
        Game g1 = gameFactory.save(b -> b.stadium(KIA)
                .homeTeam(HT).awayTeam(LT)
                .date(LocalDate.of(2025, 7, 10))
                .homeScore(8).awayScore(5)
                .gameState(GameState.COMPLETED));

        Game g2 = gameFactory.save(b -> b.stadium(KIA)
                .homeTeam(HT).awayTeam(SS)
                .date(LocalDate.of(2025, 7, 11))
                .homeScore(7).awayScore(3)
                .gameState(GameState.COMPLETED));

        Game g3 = gameFactory.save(b -> b.stadium(KIA)
                .homeTeam(HT).awayTeam(LT)
                .date(LocalDate.of(2025, 7, 12))
                .homeScore(5).awayScore(4)
                .gameState(GameState.COMPLETED));

        // 사직구장: 1승 1패
        Game g4 = gameFactory.save(b -> b.stadium(LOT)
                .homeTeam(LT).awayTeam(HT)
                .date(LocalDate.of(2025, 7, 13))
                .homeScore(4).awayScore(6) // 승
                .gameState(GameState.COMPLETED));

        Game g5 = gameFactory.save(b -> b.stadium(LOT)
                .homeTeam(LT).awayTeam(HT)
                .date(LocalDate.of(2025, 7, 14))
                .homeScore(7).awayScore(3) // 패
                .gameState(GameState.COMPLETED));

        // 라이온즈파크: 1승 0패
        Game g6 = gameFactory.save(b -> b.stadium(SAM)
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
        Stadium KIA = stadiumRepository.findById(5L).orElseThrow();

        Game g1 = gameFactory.save(b -> b.stadium(KIA)
                .homeTeam(HT).awayTeam(LT)
                .date(LocalDate.of(2025, 7, 10))
                .homeScore(6).awayScore(3) // KIA 승
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
        Stadium KIA = stadiumRepository.findById(5L).orElseThrow();
        Game g1 = gameFactory.save(b -> b.stadium(KIA)
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
        Member member = memberFactory.save(MemberBuilder::build);
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
        Stadium kiaStadium = stadiumRepository.findById(5L).orElseThrow();

        Game g1 = gameFactory.save(b -> b.stadium(kiaStadium)
                .homeTeam(HT).awayTeam(LT)
                .date(LocalDate.of(2025, 7, 10))
                .homeScore(8).awayScore(5)
                .homeScoreBoard(new ScoreBoard(8, 12, 0, 0))
                .awayScoreBoard(new ScoreBoard(5, 9, 1, 0))
                .gameState(GameState.COMPLETED));

        Game g2 = gameFactory.save(b -> b.stadium(kiaStadium)
                .homeTeam(LT).awayTeam(HT)
                .date(LocalDate.of(2025, 7, 11))
                .homeScore(4).awayScore(10)
                .homeScoreBoard(new ScoreBoard(4, 8, 0, 0))
                .awayScoreBoard(new ScoreBoard(10, 13, 0, 0))
                .gameState(GameState.COMPLETED));

        Game g3 = gameFactory.save(b -> b.stadium(kiaStadium)
                .homeTeam(HT).awayTeam(LT)
                .date(LocalDate.of(2025, 7, 12))
                .homeScore(5).awayScore(7)
                .homeScoreBoard(new ScoreBoard(5, 11, 1, 0))
                .awayScoreBoard(new ScoreBoard(7, 10, 0, 0))
                .gameState(GameState.COMPLETED));

        checkInFactory.save(b -> b.game(g1).member(member).team(HT));
        checkInFactory.save(b -> b.game(g2).member(member).team(HT));
        checkInFactory.save(b -> b.game(g3).member(member).team(HT));

        // when
        AverageStatisticResponse actual = statService.findAverageStatistic(member.getId());

        // then
        SoftAssertions.assertSoftly(softly -> {
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
}
