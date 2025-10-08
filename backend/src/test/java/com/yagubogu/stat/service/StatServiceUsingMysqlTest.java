package com.yagubogu.stat.service;

import static org.assertj.core.api.Assertions.within;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.yagubogu.auth.config.AuthTestConfig;
import com.yagubogu.checkin.repository.CheckInRepository;
import com.yagubogu.game.domain.Game;
import com.yagubogu.game.domain.GameState;
import com.yagubogu.global.config.JpaAuditingConfig;
import com.yagubogu.member.domain.Member;
import com.yagubogu.stadium.domain.Stadium;
import com.yagubogu.stadium.domain.VictoryFairyRanking;
import com.yagubogu.stadium.repository.StadiumRepository;
import com.yagubogu.stat.repository.VictoryFairyRankingRepository;
import com.yagubogu.support.base.ServiceUsingMysqlTestBase;
import com.yagubogu.support.checkin.CheckInFactory;
import com.yagubogu.support.game.GameFactory;
import com.yagubogu.support.member.MemberFactory;
import com.yagubogu.team.domain.Team;
import com.yagubogu.team.repository.TeamRepository;
import java.time.LocalDate;
import java.util.List;
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

    @DisplayName("승/패 회원 모두 랭킹이 갱신된다")
    @Test
    void calculateVictoryScore() {
        // given
        Team HT = teamRepository.findByTeamCode("HT").orElseThrow();
        Team LT = teamRepository.findByTeamCode("LT").orElseThrow();

        Member win1 = memberFactory.save(b -> b.team(HT));
        Member win2 = memberFactory.save(b -> b.team(HT));
        Member lose1 = memberFactory.save(b -> b.team(LT));

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
        checkInFactory.save(b -> b.game(game1).member(win1).team(HT));  // 승
        checkInFactory.save(b -> b.game(game2).member(win1).team(HT));  // 승
        checkInFactory.save(b -> b.game(game2).member(win2).team(HT));  // 승
        checkInFactory.save(b -> b.game(game2).member(lose1).team(LT)); // 패
        checkInFactory.save(b -> b.game(lastYearGame).member(lose1).team(LT)); // 승 -> 작년 경기이므로 영향 없음

        int lastYear = 2024;
        int year = 2025;

        // m, c 계산 (서비스 내부와 동일한 레포지토리 계산 사용)
        double m = checkInRepository.calculateTotalAverageWinRate(year);
        double c = checkInRepository.calculateAverageCheckInCount(year);

        // when
        statService.calculateVictoryScore(year, game1.getId());
        statService.calculateVictoryScore(year, game2.getId());
        statService.calculateVictoryScore(lastYear, lastYearGame.getId());

        // then
        List<VictoryFairyRanking> rankings = victoryFairyRankingRepository.findAll();
        VictoryFairyRanking rWin1 = rankings.stream()
                .filter(v -> v.getMember().getId().equals(win1.getId()) && v.getGameYear() == year)
                .findFirst().orElseThrow();
        VictoryFairyRanking rWin2 = rankings.stream()
                .filter(v -> v.getMember().getId().equals(win2.getId()) && v.getGameYear() == year)
                .findFirst().orElseThrow();
        VictoryFairyRanking rLose1 = rankings.stream()
                .filter(v -> v.getMember().getId().equals(lose1.getId()) && v.getGameYear() == year).findFirst()
                .orElseThrow();

        double expectedWinScoreByFirstRank = (2 + c * m) / (2 + c);
        double expectedWinScore = (1 + c * m) / (1 + c);
        double expectedLoseScore = (0 + c * m) / (1 + c);

        assertSoftly(s -> {
            // 승리 회원: winCount=1, checkInCount=1, 점수는 승리 공식
            s.assertThat(rWin1.getWinCount()).isEqualTo(2);
            s.assertThat(rWin1.getCheckInCount()).isEqualTo(2);
            s.assertThat(rWin1.getScore()).isCloseTo(expectedWinScoreByFirstRank, within(1e-9));

            s.assertThat(rWin2.getWinCount()).isEqualTo(1);
            s.assertThat(rWin2.getCheckInCount()).isEqualTo(1);
            s.assertThat(rWin2.getScore()).isCloseTo(expectedWinScore, within(1e-9));

            // 패배 회원: winCount=0, checkInCount=1, 점수는 패배 공식
            s.assertThat(rLose1.getWinCount()).isEqualTo(0);
            s.assertThat(rLose1.getCheckInCount()).isEqualTo(1);
            s.assertThat(rLose1.getScore()).isCloseTo(expectedLoseScore, within(1e-9));

            // 승리 점수가 패배 점수보다 커야 한다
            s.assertThat(rWin1.getScore()).isGreaterThan(rLose1.getScore());
        });
    }
}
