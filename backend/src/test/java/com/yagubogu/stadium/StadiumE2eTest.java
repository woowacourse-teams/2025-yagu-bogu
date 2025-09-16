package com.yagubogu.stadium;

import static org.assertj.core.api.Assertions.assertThat;

import com.yagubogu.game.repository.GameRepository;
import com.yagubogu.stadium.domain.Stadium;
import com.yagubogu.stadium.dto.StadiumResponse;
import com.yagubogu.stadium.dto.StadiumsResponse;
import com.yagubogu.stadium.repository.StadiumRepository;
import com.yagubogu.support.E2eTestBase;
import com.yagubogu.support.game.GameFactory;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.beans.factory.annotation.Autowired;
import com.yagubogu.team.domain.Team;
import com.yagubogu.team.repository.TeamRepository;

public class StadiumE2eTest extends E2eTestBase {

    @LocalServerPort
    private int port;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private StadiumRepository stadiumRepository;

    @Autowired
    private GameRepository gameRepository;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @DisplayName("오늘 경기가 있는 구장만 조회한다")
    @Test
    void findAllStadiums() {
        // given: 오늘 날짜에 3개의 경기 생성
        LocalDate today = LocalDate.now();
        GameFactory gameFactory = new GameFactory(gameRepository);

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
        StadiumsResponse actual = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .when().get("/api/stadiums")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(StadiumsResponse.class);

        // then
        assertThat(actual.stadiums()).containsExactlyInAnyOrderElementsOf(expected);
    }
}
