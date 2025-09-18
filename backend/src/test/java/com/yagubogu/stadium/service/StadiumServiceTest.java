package com.yagubogu.stadium.service;

import com.yagubogu.auth.config.AuthTestConfig;
import com.yagubogu.game.repository.GameRepository;
import com.yagubogu.global.config.JpaAuditingConfig;
import com.yagubogu.stadium.domain.Stadium;
import com.yagubogu.stadium.dto.StadiumResponse;
import com.yagubogu.stadium.dto.StadiumsResponse;
import com.yagubogu.stadium.repository.StadiumRepository;
import com.yagubogu.support.game.GameFactory;
import com.yagubogu.team.domain.Team;
import com.yagubogu.team.repository.TeamRepository;
import java.time.LocalDate;
import java.util.List;
import org.assertj.core.api.Assertions;
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

    @BeforeEach
    void setUp() {
        stadiumService = new StadiumService(stadiumRepository);
    }

    @DisplayName("오늘 경기가 있는 구장만 조회한다")
    @Test
    void findStadiumsWithGameStadiums() {
        // given
        LocalDate today = LocalDate.now();

        Stadium jamsil = stadiumRepository.findByShortName("잠실구장").orElseThrow();
        Stadium gocheok = stadiumRepository.findByShortName("고척돔").orElseThrow();
        Stadium landers = stadiumRepository.findByShortName("랜더스필드").orElseThrow();

        Team kia = teamRepository.findByTeamCode("HT").orElseThrow();
        Team lotte = teamRepository.findByTeamCode("LT").orElseThrow();
        Team kiwoom = teamRepository.findByTeamCode("WO").orElseThrow();
        Team hanwha = teamRepository.findByTeamCode("HH").orElseThrow();
        Team ssg = teamRepository.findByTeamCode("SK").orElseThrow();
        Team samsung = teamRepository.findByTeamCode("SS").orElseThrow();

        gameFactory.save(b -> b
                .stadium(jamsil)
                .homeTeam(kia)
                .awayTeam(lotte)
                .date(today)
        );

        gameFactory.save(b -> b
                .stadium(gocheok)
                .homeTeam(kiwoom)
                .awayTeam(hanwha)
                .date(today)
        );

        gameFactory.save(b -> b
                .stadium(landers)
                .homeTeam(ssg)
                .awayTeam(samsung)
                .date(today)
        );

        List<StadiumResponse> expected = List.of(
                StadiumResponse.from(jamsil),
                StadiumResponse.from(gocheok),
                StadiumResponse.from(landers)
        );

        // when
        StadiumsResponse actual = stadiumService.findStadiumsWithGame();

        // then
        Assertions.assertThat(actual.stadiums()).containsExactlyInAnyOrderElementsOf(expected);
    }
}
