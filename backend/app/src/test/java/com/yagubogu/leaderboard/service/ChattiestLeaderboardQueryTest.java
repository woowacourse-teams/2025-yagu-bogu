package com.yagubogu.leaderboard.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.yagubogu.auth.config.AuthTestConfig;
import com.yagubogu.game.domain.Game;
import com.yagubogu.global.config.JpaAuditingConfig;
import com.yagubogu.leaderboard.dto.LeaderboardRow;
import com.yagubogu.member.domain.Member;
import com.yagubogu.stadium.domain.Stadium;
import com.yagubogu.stadium.repository.StadiumRepository;
import com.yagubogu.support.game.GameFactory;
import com.yagubogu.support.member.MemberFactory;
import com.yagubogu.support.talk.TalkFactory;
import com.yagubogu.support.talk.TalkReportFactory;
import com.yagubogu.talk.domain.Talk;
import com.yagubogu.talk.repository.TalkRepository;
import com.yagubogu.team.domain.Team;
import com.yagubogu.team.repository.TeamRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
class ChattiestLeaderboardQueryTest {

    private ChattiestLeaderboardQuery chattiestLeaderboardQuery;

    @Autowired
    private TalkRepository talkRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private StadiumRepository stadiumRepository;

    @Autowired
    private MemberFactory memberFactory;

    @Autowired
    private GameFactory gameFactory;

    @Autowired
    private TalkFactory talkFactory;

    @Autowired
    private TalkReportFactory talkReportFactory;

    private Team kia, lg;
    private Stadium stadiumJamsil;

    @BeforeEach
    void setUp() {
        chattiestLeaderboardQuery = new ChattiestLeaderboardQuery(talkRepository);

        kia = teamRepository.findByTeamCode("HT").orElseThrow();
        lg = teamRepository.findByTeamCode("LG").orElseThrow();

        stadiumJamsil = stadiumRepository.findById(2L).orElseThrow();
    }

    @DisplayName("2025년 수다쟁이: 신고된 톡은 제외하고, 공동 1등이면 모두 rank=1로 반환한다.")
    @Test
    void findTop_excludeReported() {
        // given
        Member fora = memberFactory.save(builder -> builder.team(kia));
        Member mint = memberFactory.save(builder -> builder.team(kia));
        Member duri = memberFactory.save(builder -> builder.team(kia));

        Game game2025 = gameFactory.save(builder -> builder
                .homeTeam(kia)
                .awayTeam(lg)
                .stadium(stadiumJamsil)
                .date(LocalDate.of(2025, 7, 25)));
        Game game2024 = gameFactory.save(builder -> builder
                .homeTeam(kia)
                .awayTeam(lg)
                .stadium(stadiumJamsil)
                .date(LocalDate.of(2024, 7, 25)));

        LocalDateTime time2025 = LocalDateTime.of(2025, 7, 25, 10, 0);
        LocalDateTime time2024 = LocalDateTime.of(2024, 7, 25, 10, 0);

        // fora 2025 talk 3개 중 1개 신고 -> 공동 1등
        Talk talk1 = talkFactory.save(builder -> builder
                .member(fora)
                .game(game2025)
                .createdAt(time2025));
        Talk talk2 = talkFactory.save(builder -> builder
                .member(fora)
                .game(game2025)
                .createdAt(time2025));
        Talk talk3 = talkFactory.save(builder -> builder
                .member(fora)
                .game(game2025)
                .createdAt(time2025));
        talkReportFactory.save(builder -> builder
                .talk(talk3)
                .reporter(fora)
        );

        // mint 2025 talk 2개 -> 공동 1등
        talkFactory.save(builder -> builder
                .member(mint)
                .game(game2025)
                .createdAt(time2025));
        talkFactory.save(builder -> builder
                .member(mint)
                .game(game2025)
                .createdAt(time2025));

        // duri 2025 talk 1개 -> 2등
        talkFactory.save(builder -> builder
                .member(duri)
                .game(game2025)
                .createdAt(time2025));

        // 2024 talk 집계 제외
        talkFactory.save(builder -> builder
                .member(fora)
                .game(game2024)
                .createdAt(time2024));
        talkFactory.save(builder -> builder
                .member(fora)
                .game(game2024)
                .createdAt(time2024));

        // when
        List<LeaderboardRow> rows = chattiestLeaderboardQuery.findTop(1);

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
        Member fora = memberFactory.save(builder -> builder.team(kia));
        Member mint = memberFactory.save(builder -> builder.team(kia));
        Member duri = memberFactory.save(builder -> builder.team(kia));

        Game game2025 = gameFactory.save(builder -> builder
                .homeTeam(kia)
                .awayTeam(lg)
                .stadium(stadiumJamsil)
                .date(LocalDate.of(2025, 7, 25)));

        LocalDateTime time2025 = LocalDateTime.of(2025, 7, 25, 10, 0);
        talkFactory.save(builder -> builder.member(fora).game(game2025).createdAt(time2025));
        talkFactory.save(builder -> builder.member(fora).game(game2025).createdAt(time2025));
        talkFactory.save(builder -> builder.member(mint).game(game2025).createdAt(time2025));
        talkFactory.save(builder -> builder.member(mint).game(game2025).createdAt(time2025));
        talkFactory.save(builder -> builder.member(duri).game(game2025).createdAt(time2025));

        // when
        List<LeaderboardRow> rows = chattiestLeaderboardQuery.findTop(2);

        // then
        assertThat(rows).hasSize(3);
        assertThat(rows).extracting(LeaderboardRow::rank).contains(1L, 2L);
        assertThat(rows).extracting(LeaderboardRow::memberId)
                .containsExactlyInAnyOrder(fora.getId(), mint.getId(), duri.getId());
    }
}
