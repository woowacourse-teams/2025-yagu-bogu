package com.yagubogu.stadium.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.yagubogu.auth.config.AuthTestConfig;
import com.yagubogu.game.domain.Game;
import com.yagubogu.game.repository.GameRepository;
import com.yagubogu.global.config.JpaAuditingConfig;
import com.yagubogu.stadium.domain.Stadium;
import com.yagubogu.stadium.dto.v1.StadiumsWithGamesResponse;
import com.yagubogu.stadium.repository.StadiumRepository;
import com.yagubogu.support.TestFixture;
import com.yagubogu.support.game.GameFactory;
import com.yagubogu.team.domain.Team;
import com.yagubogu.team.repository.TeamRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@Import({AuthTestConfig.class, JpaAuditingConfig.class})
@DataJpaTest
class StadiumServiceTest {

    private StadiumService stadiumService;

    @Autowired
    private StadiumRepository stadiumRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private GameFactory gameFactory;

    private Stadium stadiumJamsil, stadiumGocheok, stadiumIncheon;
    private Team kia, kt, lg, samsung, doosan, lotte;

    @BeforeEach
    void setUp() {
        stadiumService = new StadiumService(gameRepository);

        stadiumJamsil = stadiumRepository.findById(2L).orElseThrow();
        stadiumGocheok = stadiumRepository.findById(3L).orElseThrow();
        stadiumIncheon = stadiumRepository.findById(7L).orElseThrow();

        kia = teamRepository.findByTeamCode("HT").orElseThrow();
        kt = teamRepository.findByTeamCode("KT").orElseThrow();
        lg = teamRepository.findByTeamCode("LG").orElseThrow();
        samsung = teamRepository.findByTeamCode("SS").orElseThrow();
        doosan = teamRepository.findByTeamCode("OB").orElseThrow();
        lotte = teamRepository.findByTeamCode("LT").orElseThrow();
    }

    @DisplayName("해당 날짜에 경기가 있는 구장들과 각 경기들을 조회한다")
    @Test
    void findStadiumsWithGameByDate() {
        // given
        LocalDate date = TestFixture.getToday();

        Game game1 = makeGame(date, LocalTime.of(18, 30), kia, lotte, stadiumJamsil);
        Game game2 = makeGame(date, LocalTime.of(18, 30), doosan, kt, stadiumGocheok);
        Game game3 = makeGame(date, LocalTime.of(2, 30), kt, samsung, stadiumIncheon);
        Game game4 = makeGame(date, LocalTime.of(18, 30), kt, samsung, stadiumIncheon);

        LocalDate invalidDate = date.minusDays(1);
        Game game5 = makeGame(invalidDate, LocalTime.of(18, 30), kt, samsung, stadiumIncheon);

        Map<Stadium, List<Game>> map = Map.of(stadiumJamsil, List.of(game1), stadiumGocheok, List.of(game2),
                stadiumIncheon, List.of(game3, game4));
        StadiumsWithGamesResponse expected = StadiumsWithGamesResponse.from(map);

        // when
        StadiumsWithGamesResponse actual = stadiumService.findWithGameByDate(date);

        // then
        assertThat(actual.stadiums()).containsExactlyInAnyOrderElementsOf(expected.stadiums());
    }

    private Game makeGame(LocalDate date, LocalTime startAt, Team home, Team away, Stadium stadium) {
        return gameFactory.save(builder -> builder
                .homeTeam(home)
                .awayTeam(away)
                .stadium(stadium)
                .date(date)
                .startAt(startAt)
        );
    }
}
