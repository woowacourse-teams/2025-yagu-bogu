package com.yagubogu.stat.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.yagubogu.auth.config.AuthTestConfig;
import com.yagubogu.checkin.domain.CheckInType;
import com.yagubogu.game.domain.Game;
import com.yagubogu.game.domain.GameState;
import com.yagubogu.global.config.JpaAuditingConfig;
import com.yagubogu.member.domain.Member;
import com.yagubogu.stadium.domain.Stadium;
import com.yagubogu.stadium.repository.StadiumRepository;
import com.yagubogu.stat.domain.VictoryFairyRanking;
import com.yagubogu.stat.repository.VictoryFairyRankingRepository;
import com.yagubogu.support.base.ServiceUsingMysqlTestBase;
import com.yagubogu.support.checkin.CheckInFactory;
import com.yagubogu.support.game.GameFactory;
import com.yagubogu.support.member.MemberFactory;
import com.yagubogu.team.domain.Team;
import com.yagubogu.team.repository.TeamRepository;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@Import({AuthTestConfig.class, JpaAuditingConfig.class})
class StatSyncServiceUsingMysqlTest extends ServiceUsingMysqlTestBase {

    @Autowired
    private StatSyncService statSyncService;

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

    private Team HT, LT;
    private Stadium kia;

    @BeforeEach
    void setUp() {
        HT = teamRepository.findByTeamCode("HT").orElseThrow();
        LT = teamRepository.findByTeamCode("LT").orElseThrow();
        kia = stadiumRepository.findByShortName("챔피언스필드").orElseThrow();
    }

    @DisplayName("해당 날짜에 체크인한 회원들의 랭킹이 최초 삽입된다")
    @Test
    void updateRankings_insert() {
        // given
        LocalDate date = LocalDate.of(2025, 7, 21);
        int year = date.getYear();

        Member m1 = memberFactory.save(b -> b.team(HT));
        Member m2 = memberFactory.save(b -> b.team(HT));

        Game g1 = gameFactory.save(b -> b.stadium(kia)
                .homeTeam(HT).awayTeam(LT)
                .date(date)
                .homeScore(5).awayScore(3)
                .gameState(GameState.COMPLETED));

        checkInFactory.save(b -> b.game(g1).member(m1).team(HT));
        checkInFactory.save(b -> b.game(g1).member(m2).team(HT));

        // when
        statSyncService.updateRankings(date);

        // then
        List<VictoryFairyRanking> results = victoryFairyRankingRepository
                .findByMemberIdsAndYear(List.of(m1.getId(), m2.getId()), year);

        assertSoftly(softAssertions -> {
            softAssertions.assertThat(results).hasSize(2);
            softAssertions.assertThat(results);
            softAssertions.assertThat(results).extracting(VictoryFairyRanking::getWinCount)
                    .containsExactlyInAnyOrder(1, 1);
            softAssertions.assertThat(results).extracting(VictoryFairyRanking::getCheckInCount)
                    .containsExactlyInAnyOrder(1, 1);
            softAssertions.assertThat(results).extracting(VictoryFairyRanking::getScore)
                    .containsExactlyInAnyOrder(100.0, 100.0);
        });
    }

    @DisplayName("기존 연도 데이터가 있을 때, 배치가 돌면 전체 평균 변동에 따라 직관을 안 간 회원의 점수도 재계산된다") // DisplayName 변경
    @Test
    void updateRankings_updateExisting() {
        // given
        LocalDate date = LocalDate.of(2025, 7, 21);
        int year = date.getYear();

        Member m1 = memberFactory.save(b -> b.team(HT));
        Member m2 = memberFactory.save(b -> b.team(HT));

        // 초기 삽입용 경기 (7/21 HT 승)
        Game g1 = gameFactory.save(b -> b.stadium(kia)
                .homeTeam(HT).awayTeam(LT)
                .date(date)
                .homeScore(5).awayScore(3)
                .gameState(GameState.COMPLETED));
        checkInFactory.save(b -> b.game(g1).member(m1).team(HT));
        checkInFactory.save(b -> b.game(g1).member(m2).team(HT));
        statSyncService.updateRankings(date);

        // 누적용 경기 (8/1 HT 패, m1만 체크인)
        LocalDate later = LocalDate.of(2025, 8, 1);
        Game g2 = gameFactory.save(b -> b.stadium(kia)
                .homeTeam(HT).awayTeam(LT)
                .date(later)
                .homeScore(1).awayScore(6)
                .gameState(GameState.COMPLETED));
        checkInFactory.save(b -> b.game(g2).member(m1).team(HT));

        // when (8월 1일자로 배치 실행 -> 2025년도 전체 업데이트)
        statSyncService.updateRankings(later);

        // then
        List<VictoryFairyRanking> results = victoryFairyRankingRepository
                .findByMemberIdsAndYear(List.of(m1.getId(), m2.getId()), year);

        VictoryFairyRanking r1 = results.stream().filter(r -> r.getMember().getId().equals(m1.getId())).findFirst()
                .orElseThrow();
        VictoryFairyRanking r2 = results.stream().filter(r -> r.getMember().getId().equals(m2.getId())).findFirst()
                .orElseThrow();

        assertSoftly(softAssertions -> {
            // m1 검증
            softAssertions.assertThat(r1.getWinCount()).isEqualTo(1);
            softAssertions.assertThat(r1.getCheckInCount()).isEqualTo(2);
            softAssertions.assertThat(r1.getScore()).isEqualTo(57.14);

            // m2 검증 (주석 해제 및 올바른 보정 점수 기입)
            softAssertions.assertThat(r2.getWinCount()).isEqualTo(1);
            softAssertions.assertThat(r2.getCheckInCount()).isEqualTo(1);
            softAssertions.assertThat(r2.getScore()).isEqualTo(80.0); // 100점에서 전체 평균 하락으로 인해 80점으로 보정됨
        });
    }

    @DisplayName("해당 날짜에 체크인한 회원이 없으면 변화가 없다")
    @Test
    void updateRankings_noMembers() {
        // given
        LocalDate date = LocalDate.of(2025, 7, 22); // 아무도 체크인하지 않은 날짜

        // when
        statSyncService.updateRankings(date);

        // then
        assertThat(victoryFairyRankingRepository.findAll()).isEmpty();
    }

    @DisplayName("과거 직관(NON_LOCATION_CHECK_IN)도 승리요정 랭킹 점수 계산에 포함된다")
    @Test
    void updateRankings_includesPastCheckIn() {
        // given
        LocalDate date = LocalDate.of(2025, 7, 21);
        int year = date.getYear();

        Member member = memberFactory.save(b -> b.team(HT));

        // 일반 직관: HT 승
        Game normalGame = gameFactory.save(b -> b.stadium(kia)
                .homeTeam(HT).awayTeam(LT)
                .date(date)
                .homeScore(5).awayScore(3)
                .gameState(GameState.COMPLETED));
        checkInFactory.save(b -> b.game(normalGame).member(member).team(HT));

        // 과거 직관: HT 승 (랭킹에도 포함되어야 함)
        Game pastGame = gameFactory.save(b -> b.stadium(kia)
                .homeTeam(HT).awayTeam(LT)
                .date(LocalDate.of(2025, 6, 1))
                .homeScore(7).awayScore(2)
                .gameState(GameState.COMPLETED));
        checkInFactory.save(b -> b.game(pastGame).member(member).team(HT)
                .checkInType(CheckInType.NON_LOCATION_CHECK_IN));

        // when
        statSyncService.updateRankings(date);

        // then: 과거 직관 포함, 총 2회 직관 반영 (checkInCount=2, winCount=2)
        List<VictoryFairyRanking> results = victoryFairyRankingRepository
                .findByMemberIdsAndYear(List.of(member.getId()), year);

        assertSoftly(softAssertions -> {
            softAssertions.assertThat(results).hasSize(1);
            softAssertions.assertThat(results.get(0).getCheckInCount()).isEqualTo(2);
            softAssertions.assertThat(results.get(0).getWinCount()).isEqualTo(2);
        });
    }

    @DisplayName("과거 직관(NON_LOCATION_CHECK_IN)만 있는 회원도 승리요정 랭킹에 등록된다")
    @Test
    void updateRankings_memberWithOnlyPastCheckIn_isRanked() {
        // given
        LocalDate date = LocalDate.of(2025, 7, 21);
        int year = date.getYear();

        Member normalMember = memberFactory.save(b -> b.team(HT));
        Member pastOnlyMember = memberFactory.save(b -> b.team(HT));

        // normalMember: 일반 직관 (해당 날짜)
        Game normalGame = gameFactory.save(b -> b.stadium(kia)
                .homeTeam(HT).awayTeam(LT)
                .date(date)
                .homeScore(5).awayScore(3)
                .gameState(GameState.COMPLETED));
        checkInFactory.save(b -> b.game(normalGame).member(normalMember).team(HT));

        // pastOnlyMember: 과거 직관만 존재 (랭킹에도 포함되어야 함)
        Game pastGame = gameFactory.save(b -> b.stadium(kia)
                .homeTeam(HT).awayTeam(LT)
                .date(LocalDate.of(2025, 6, 1))
                .homeScore(7).awayScore(2)
                .gameState(GameState.COMPLETED));
        checkInFactory.save(b -> b.game(pastGame).member(pastOnlyMember).team(HT)
                .checkInType(CheckInType.NON_LOCATION_CHECK_IN));

        // when
        statSyncService.updateRankings(date);

        // then: normalMember와 pastOnlyMember 모두 랭킹에 등록
        List<VictoryFairyRanking> results = victoryFairyRankingRepository
                .findByMemberIdsAndYear(List.of(normalMember.getId(), pastOnlyMember.getId()), year);

        assertSoftly(softAssertions -> {
            softAssertions.assertThat(results).hasSize(2);
            softAssertions.assertThat(results)
                    .extracting(r -> r.getMember().getId())
                    .containsExactlyInAnyOrder(normalMember.getId(), pastOnlyMember.getId());
        });
    }

    @DisplayName("다른 연도의 직관 기록은 당해 연도 랭킹 업데이트에 영향을 주지 않는다")
    @Test
    void updateRankings_separatesYears() {
        // given
        Member m1 = memberFactory.save(b -> b.team(HT));

        // 2024년 직관 (HT 승)
        LocalDate date2024 = LocalDate.of(2024, 5, 5);
        Game g2024 = gameFactory.save(b -> b.stadium(kia)
                .homeTeam(HT).awayTeam(LT).date(date2024)
                .homeScore(5).awayScore(3).gameState(GameState.COMPLETED));
        checkInFactory.save(b -> b.game(g2024).member(m1).team(HT));
        statSyncService.updateRankings(date2024); // 2024년 배치 1회 실행

        // 2025년 직관 (HT 승, 다른 멤버)
        Member m2 = memberFactory.save(b -> b.team(HT));
        LocalDate date2025 = LocalDate.of(2025, 5, 5);
        Game g2025 = gameFactory.save(b -> b.stadium(kia)
                .homeTeam(HT).awayTeam(LT).date(date2025)
                .homeScore(5).awayScore(3).gameState(GameState.COMPLETED));
        checkInFactory.save(b -> b.game(g2025).member(m2).team(HT));

        // when (2025년 기준으로 배치 실행)
        statSyncService.updateRankings(date2025);

        // then
        // 1. 2025년 랭킹에는 2025년에 직관한 m2만 등록되어야 함
        List<VictoryFairyRanking> results2025 = victoryFairyRankingRepository
                .findByMemberIdsAndYear(List.of(m1.getId(), m2.getId()), 2025);

        // 2. 2024년 랭킹의 m1 점수는 2025년 데이터에 영향을 받지 않고 그대로 있어야 함
        List<VictoryFairyRanking> results2024 = victoryFairyRankingRepository
                .findByMemberIdsAndYear(List.of(m1.getId()), 2024);

        assertSoftly(softAssertions -> {
            // 2025년 검증
            softAssertions.assertThat(results2025).hasSize(1);
            softAssertions.assertThat(results2025.get(0).getMember().getId()).isEqualTo(m2.getId());
            softAssertions.assertThat(results2025.get(0).getScore()).isEqualTo(100.0);

            // 2024년 검증
            softAssertions.assertThat(results2024).hasSize(1);
            softAssertions.assertThat(results2024.get(0).getScore()).isEqualTo(100.0);
        });
    }
}
