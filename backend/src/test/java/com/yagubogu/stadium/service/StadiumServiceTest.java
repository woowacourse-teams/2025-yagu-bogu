package com.yagubogu.stadium.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.yagubogu.auth.config.AuthTestConfig;
import com.yagubogu.game.domain.Game;
import com.yagubogu.global.config.JpaAuditingConfig;
import com.yagubogu.stadium.domain.Stadium;
import com.yagubogu.stadium.dto.StadiumParam;
import com.yagubogu.stadium.dto.StadiumsWithGamesResponse;
import com.yagubogu.stadium.dto.v1.StadiumsResponse;
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
    private GameFactory gameFactory;

    private Stadium stadiumJamsil, stadiumGocheok, stadiumIncheon;
    private Team kia, kt, lg, samsung, doosan, lotte;

    @BeforeEach
    void setUp() {
        stadiumService = new StadiumService(stadiumRepository);

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

    @DisplayName("제 2 구장을 제외한 전체 구장 목록을 조회한다")
    @Test
    void findAllMainStadiumsStadiums() {
        // given
        List<StadiumParam> expected = List.of(
                new StadiumParam(1L, "광주 기아 챔피언스필드", "챔피언스필드", "광주", 35.168139, 126.889111),
                new StadiumParam(2L, "잠실 야구장", "잠실구장", "잠실", 37.512150, 127.071976),
                new StadiumParam(3L, "고척 스카이돔", "고척돔", "고척", 37.498222, 126.867250),
                new StadiumParam(4L, "수원 KT 위즈파크", "위즈파크", "수원", 37.299759, 127.009781),
                new StadiumParam(5L, "대구 삼성 라이온즈파크", "라이온즈파크", "대구", 35.841111, 128.681667),
                new StadiumParam(6L, "사직야구장", "사직구장", "사직", 35.194077, 129.061584),
                new StadiumParam(7L, "인천 SSG 랜더스필드", "랜더스필드", "문학", 37.436778, 126.693306),
                new StadiumParam(8L, "창원 NC 파크", "엔씨파크", "창원", 35.222754, 128.582251),
                new StadiumParam(9L, "대전 한화생명 볼파크", "볼파크", "대전", 36.316589, 127.431211)
        );

        // when
        StadiumsResponse actual = stadiumService.findAllMainStadiums();

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

        LocalDate invalidDate = date.minusDays(1);
        Game game5 = makeGame(invalidDate, LocalTime.of(18, 30), kt, samsung, stadiumIncheon);

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
