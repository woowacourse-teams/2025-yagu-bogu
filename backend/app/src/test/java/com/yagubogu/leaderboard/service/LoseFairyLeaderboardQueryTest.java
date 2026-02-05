package com.yagubogu.leaderboard.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.yagubogu.auth.config.AuthTestConfig;
import com.yagubogu.global.config.JpaAuditingConfig;
import com.yagubogu.leaderboard.dto.LeaderboardRow;
import com.yagubogu.member.domain.Member;
import com.yagubogu.stat.repository.VictoryFairyRankingRepository;
import com.yagubogu.support.member.MemberFactory;
import com.yagubogu.team.domain.Team;
import com.yagubogu.team.repository.TeamRepository;
import java.time.Year;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

@ExtendWith(MockitoExtension.class)
@Import({AuthTestConfig.class, JpaAuditingConfig.class})
@DataJpaTest
class LoseFairyLeaderboardQueryTest {

    private LoseFairyLeaderboardQuery loseFairyLeaderboardQuery;

    @Autowired
    VictoryFairyRankingRepository victoryFairyRankingRepository;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    TeamRepository teamRepository;

    @Autowired
    private MemberFactory memberFactory;

    private Team kia, lg;

    @BeforeEach
    void setUp() {
        loseFairyLeaderboardQuery = new LoseFairyLeaderboardQuery(victoryFairyRankingRepository);

        kia = teamRepository.findByTeamCode("HT").orElseThrow();
        lg = teamRepository.findByTeamCode("LG").orElseThrow();
    }

    @DisplayName("패배요정 랭킹은 lastYear 기준 score 오름차순으로 Top1을 반환한다.")
    @Test
    void findTop_lastYear_top1() {
        // given
        int lastYear = Year.now().minusYears(1).getValue();
        Member fora = memberFactory.save(builder -> builder.team(kia));
        Member mint = memberFactory.save(builder -> builder.team(lg));

        insertVictoryFairyRanking(fora.getId(), lastYear, 90.0);
        insertVictoryFairyRanking(mint.getId(), lastYear, 78.0);

        // when
        List<LeaderboardRow> rows = loseFairyLeaderboardQuery.findTop(1);

        // then
        assertThat(rows).hasSize(1);
        LeaderboardRow top = rows.get(0);

        assertThat(top.rank()).isEqualTo(1L);
        assertThat(top.memberId()).isEqualTo(mint.getId());
        assertThat(top.favoriteTeam()).isEqualTo(lg.getShortName());
        assertThat(top.score()).isEqualTo(78.0);
    }

    @DisplayName("패배요정 명예의 전당에서 다른 연도 데이터는 집계에서 제외된다.")
    @Test
    void findTop_excludeOtherYear() {
        // given
        int lastYear = Year.now().minusYears(1).getValue();
        Member fora = memberFactory.save(builder -> builder.team(kia));
        Member mint = memberFactory.save(builder -> builder.team(lg));

        insertVictoryFairyRanking(fora.getId(), lastYear - 1, 16.0);
        insertVictoryFairyRanking(mint.getId(), lastYear, 99.0);

        // when
        List<LeaderboardRow> rows = loseFairyLeaderboardQuery.findTop(1);

        // then
        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).memberId()).isEqualTo(mint.getId());
        assertThat(rows.get(0).score()).isEqualTo(99.0);
    }

    @DisplayName("패배요정 명예의 전당에서 limit=2이면 1등~2등까지 반환한다.")
    @Test
    void findTop_limit2() {
        // given
        int lastYear = Year.now().minusYears(1).getValue(); // 2025
        Member fora = memberFactory.save(b -> b.team(kia));
        Member mint = memberFactory.save(b -> b.team(kia));
        Member duri = memberFactory.save(b -> b.team(kia));

        insertVictoryFairyRanking(fora.getId(), lastYear, 63.0);
        insertVictoryFairyRanking(mint.getId(), lastYear, 86.0);
        insertVictoryFairyRanking(duri.getId(), lastYear, 94.0);

        // when
        List<LeaderboardRow> rows = loseFairyLeaderboardQuery.findTop(2);

        // then
        assertThat(rows).hasSize(2);
        assertThat(rows).extracting(LeaderboardRow::rank).containsExactly(1L, 2L);
        assertThat(rows).extracting(LeaderboardRow::memberId)
                .containsExactly(fora.getId(), mint.getId());
    }

    private void insertVictoryFairyRanking(long memberId, int gameYear, double score) {
        jdbcTemplate.update("""
                INSERT INTO victory_fairy_rankings
                  (member_id, score, win_count, check_in_count, game_year, updated_at)
                VALUES
                  (?, ?, 0, 0, ?, NULL)
                """, memberId, score, gameYear);
    }
}
