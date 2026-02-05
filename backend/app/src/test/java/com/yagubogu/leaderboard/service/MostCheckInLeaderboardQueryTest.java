package com.yagubogu.leaderboard.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.yagubogu.auth.config.AuthTestConfig;
import com.yagubogu.checkin.domain.CheckInType;
import com.yagubogu.checkin.repository.CheckInRepository;
import com.yagubogu.game.domain.Game;
import com.yagubogu.global.config.JpaAuditingConfig;
import com.yagubogu.leaderboard.dto.LeaderboardRow;
import com.yagubogu.member.domain.Member;
import com.yagubogu.stadium.domain.Stadium;
import com.yagubogu.stadium.repository.StadiumRepository;
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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@ExtendWith(MockitoExtension.class)
@Import({AuthTestConfig.class, JpaAuditingConfig.class})
@DataJpaTest
class MostCheckInLeaderboardQueryTest {

    private MostCheckInLeaderboardQuery mostCheckInLeaderboardQuery;

    @Autowired
    private CheckInRepository checkInRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private StadiumRepository stadiumRepository;

    @Autowired
    private MemberFactory memberFactory;

    @Autowired
    private GameFactory gameFactory;

    @Autowired
    private CheckInFactory checkInFactory;

    private Team kia, lg, kt;
    private Stadium stadiumJamsil;

    @BeforeEach
    void setUp() {
        mostCheckInLeaderboardQuery = new MostCheckInLeaderboardQuery(checkInRepository);

        kia = teamRepository.findByTeamCode("HT").orElseThrow();
        lg = teamRepository.findByTeamCode("LG").orElseThrow();
        kt = teamRepository.findByTeamCode("KT").orElseThrow();

        stadiumJamsil = stadiumRepository.findById(2L).orElseThrow();
    }

    @DisplayName("최다직관 명예의 전당에서 공동 1등이면 rank=1로 반환한다.")
    @Test
    void findTop_rank1_allReturned() {
        // given
        Member fora = memberFactory.save(b -> b.team(kia).nickname("포라"));
        Member mint = memberFactory.save(b -> b.team(lg).nickname("밍트"));
        Member duri = memberFactory.save(b -> b.team(kt).nickname("두리"));

        Game game2025 = gameFactory.save(b -> b
                .homeTeam(kia).awayTeam(lg)
                .stadium(stadiumJamsil)
                .date(LocalDate.of(2025, 7, 25))
        );
        Game game2024 = gameFactory.save(b -> b
                .homeTeam(kia).awayTeam(lg)
                .stadium(stadiumJamsil)
                .date(LocalDate.of(2024, 7, 25))
        );

        // fora 2025 체크인 2개 (공동 1등)
        checkIn(fora, game2025);
        checkIn(fora, gameFactory.save(
                b -> b.homeTeam(kia).awayTeam(kt).stadium(stadiumJamsil).date(LocalDate.of(2025, 7, 26))));

        // mint 2025 체크인 2개 (공동 1등)
        checkIn(mint, game2025);
        checkIn(mint, gameFactory.save(
                b -> b.homeTeam(lg).awayTeam(kt).stadium(stadiumJamsil).date(LocalDate.of(2025, 7, 27))));

        // duri 2025 체크인 1개 (2등)
        checkIn(duri, game2025);

        // 2024 체크인은 집계 제외되어야 함
        checkIn(fora, game2024);
        checkIn(duri, game2024);

        // when
        List<LeaderboardRow> rows = mostCheckInLeaderboardQuery.findTop(1);

        // then
        assertThat(rows).hasSize(2);
        assertThat(rows).allMatch(r -> r.rank() == 1L);
        assertThat(rows).extracting(LeaderboardRow::memberId)
                .containsExactlyInAnyOrder(fora.getId(), mint.getId());
        assertThat(rows).allMatch(r -> r.score() == 2.0);
    }

    @DisplayName("limit=2이면 공동 1등 + 2등까지 포함된다.")
    @Test
    void findTop_limit2_includeRank2() {
        // given
        Member fora = memberFactory.save(b -> b.team(kia).nickname("포라"));
        Member mint = memberFactory.save(b -> b.team(lg).nickname("밍트"));
        Member duri = memberFactory.save(b -> b.team(kt).nickname("두리"));

        Game g1 = gameFactory.save(
                b -> b.homeTeam(kia).awayTeam(lg).stadium(stadiumJamsil).date(LocalDate.of(2025, 7, 25)));
        Game g2 = gameFactory.save(
                b -> b.homeTeam(kia).awayTeam(kt).stadium(stadiumJamsil).date(LocalDate.of(2025, 7, 26)));

        // fora 2회
        checkIn(fora, g1);
        checkIn(fora, g2);

        // mint 2회 (공동 1등)
        checkIn(mint, g1);
        checkIn(mint, g2);

        // duri 1회 (2등)
        checkIn(duri, g1);

        // when
        List<LeaderboardRow> rows = mostCheckInLeaderboardQuery.findTop(2);

        // then
        assertThat(rows).hasSize(3);
        assertThat(rows).extracting(LeaderboardRow::rank).contains(1L, 2L);
        assertThat(rows).extracting(LeaderboardRow::memberId)
                .containsExactlyInAnyOrder(fora.getId(), mint.getId(), duri.getId());
        assertThat(rows.stream().filter(r -> r.memberId() == duri.getId()).findFirst().orElseThrow().score())
                .isEqualTo(1.0);
    }

    private void checkIn(Member member, Game game) {
        checkInFactory.save(builder -> builder
                .member(member)
                .game(game)
                .team(member.getTeam())
                .checkInType(CheckInType.NON_LOCATION_CHECK_IN)
        );
    }
}
