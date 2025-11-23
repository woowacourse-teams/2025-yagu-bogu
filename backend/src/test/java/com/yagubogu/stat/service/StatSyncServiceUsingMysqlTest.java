package com.yagubogu.stat.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.yagubogu.auth.config.AuthTestConfig;
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
        statSyncService.updateRankings(date, 1000, 1000);

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

    @DisplayName("기존 연도 데이터가 있을 때, 같은 날짜로 갱신 시 누적 값으로 업데이트된다")
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
        statSyncService.updateRankings(date, 1000, 1000);

        // 누적용 경기 (8/1 HT 패, m1만 체크인)
        LocalDate later = LocalDate.of(2025, 8, 1);
        Game g2 = gameFactory.save(b -> b.stadium(kia)
                .homeTeam(HT).awayTeam(LT)
                .date(later)
                .homeScore(1).awayScore(6)
                .gameState(GameState.COMPLETED));
        checkInFactory.save(b -> b.game(g2).member(m1).team(HT));

        // when
        statSyncService.updateRankings(later, 1000, 1000);

        // then
        List<VictoryFairyRanking> results = victoryFairyRankingRepository
                .findByMemberIdsAndYear(List.of(m1.getId(), m2.getId()), year);

        VictoryFairyRanking r1 = results.stream().filter(r -> r.getMember().getId().equals(m1.getId())).findFirst()
                .orElseThrow();
        VictoryFairyRanking r2 = results.stream().filter(r -> r.getMember().getId().equals(m2.getId())).findFirst()
                .orElseThrow();

        assertSoftly(softAssertions -> {
            softAssertions.assertThat(r1.getWinCount()).isEqualTo(1);
            softAssertions.assertThat(r1.getCheckInCount()).isEqualTo(2);
            softAssertions.assertThat(r1.getScore()).isEqualTo(57.14);
            softAssertions.assertThat(r2.getWinCount()).isEqualTo(1);
            softAssertions.assertThat(r2.getCheckInCount()).isEqualTo(1);
            softAssertions.assertThat(r2.getScore()).isEqualTo(100.0);
        });
    }

    @DisplayName("해당 날짜에 체크인한 회원이 없으면 변화가 없다")
    @Test
    void updateRankings_noMembers() {
        // given
        LocalDate date = LocalDate.of(2025, 7, 22); // 아무도 체크인하지 않은 날짜

        // when
        statSyncService.updateRankings(date, 1000, 1000);

        // then
        assertThat(victoryFairyRankingRepository.findAll()).isEmpty();
    }
}

