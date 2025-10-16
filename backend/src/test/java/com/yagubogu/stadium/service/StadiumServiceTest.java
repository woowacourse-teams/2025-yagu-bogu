package com.yagubogu.stadium.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.yagubogu.auth.config.AuthTestConfig;
import com.yagubogu.game.domain.Game;
import com.yagubogu.game.repository.GameRepository;
import com.yagubogu.global.config.JpaAuditingConfig;
import com.yagubogu.stadium.domain.Stadium;
import com.yagubogu.stadium.dto.StadiumResponse;
import com.yagubogu.stadium.dto.StadiumsResponse;
import com.yagubogu.stadium.dto.StadiumsWithGamesResponse;
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
    private GameRepository gameRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private GameFactory gameFactory;

    private Stadium stadiumJamsil, stadiumGocheok, stadiumIncheon;
    private Team kia, kt, lg, samsung, doosan, lotte;

    @BeforeEach
    void setUp() {
        stadiumService = new StadiumService(stadiumRepository, gameRepository);

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

    @DisplayName("전체 구장 목록을 조회한다")
    @Test
    void findAllStadiums() {
        // given
        List<StadiumResponse> expected = List.of(
                new StadiumResponse(1L, "챔피언스필드", "챔피언스필드", "광주", 35.1683, 126.8889),
                new StadiumResponse(2L, "잠실야구장", "잠실구장", "잠실", 37.5121, 127.0710),
                new StadiumResponse(3L, "고척스카이돔", "고척돔", "고척", 37.4982, 126.8676),
                new StadiumResponse(4L, "수원KT위즈파크", "위즈파크", "수원", 37.2996, 126.9707),
                new StadiumResponse(5L, "대구삼성라이온즈파크", "라이온즈파크", "대구", 35.8419, 128.6815),
                new StadiumResponse(6L, "사직야구장", "사직구장", "부산", 35.1943, 129.0615),
                new StadiumResponse(7L, "문학야구장", "랜더스필드", "인천", 37.4361, 126.6892),
                new StadiumResponse(8L, "마산야구장", "엔씨파크", "마산", 35.2281, 128.6819),
                new StadiumResponse(9L, "이글스파크", "볼파크", "대전", 36.3173, 127.4280)
        );

        // when
        StadiumsResponse actual = stadiumService.findAll();

        // then
        assertThat(actual.stadiums()).isEqualTo(expected);
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

        // 해당 날짜가 아닌 경기는 포함되지 않는다
        Game game5 = makeGame(date.minusDays(1), LocalTime.of(18, 30), kt, samsung, stadiumIncheon);

        Map<Stadium, List<Game>> map = Map.of(stadiumJamsil, List.of(game1), stadiumGocheok, List.of(game2),
                stadiumIncheon, List.of(game3, game4));
        StadiumsWithGamesResponse expected = StadiumsWithGamesResponse.from(map);

        // when
        StadiumsWithGamesResponse actual = stadiumService.findWithGameByDate(date);

        // then
        assertThat(actual).isEqualTo(expected);
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
