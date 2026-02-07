package com.yagubogu.leaderboard;

import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;

import com.yagubogu.auth.config.AuthTestConfig;
import com.yagubogu.game.domain.Game;
import com.yagubogu.game.domain.GameState;
import com.yagubogu.global.config.JpaAuditingConfig;
import com.yagubogu.leaderboard.domain.LeaderboardType;
import com.yagubogu.leaderboard.dto.LeaderboardItemResponse;
import com.yagubogu.leaderboard.dto.LeaderboardResponse;
import com.yagubogu.member.domain.Member;
import com.yagubogu.member.domain.Role;
import com.yagubogu.stadium.domain.Stadium;
import com.yagubogu.stadium.repository.StadiumRepository;
import com.yagubogu.support.TestFixture;
import com.yagubogu.support.auth.AuthFactory;
import com.yagubogu.support.base.E2eTestBase;
import com.yagubogu.support.checkin.CheckInFactory;
import com.yagubogu.support.game.GameFactory;
import com.yagubogu.support.member.MemberFactory;
import com.yagubogu.team.domain.Team;
import com.yagubogu.team.repository.TeamRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;

@Import({AuthTestConfig.class, JpaAuditingConfig.class})
public class LeaderboardE2eTest extends E2eTestBase {

    @LocalServerPort
    private int port;

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
    private AuthFactory authFactory;

    private Team kia, lotte;
    private Stadium stadiumJamsil, stadiumGocheok;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;

        kia = teamRepository.findByTeamCode("HT").orElseThrow();
        lotte = teamRepository.findByTeamCode("LT").orElseThrow();

        stadiumJamsil = stadiumRepository.findById(2L).orElseThrow();
        stadiumGocheok = stadiumRepository.findById(3L).orElseThrow();
    }

    @DisplayName("명예의 전당 전체 조회는 각 타입 TopN을 반환한다")
    @Test
    void getAllLeaderboards() {
        // given: 2025년 데이터로 MOST_CHECK_IN 1위 만들기
        Member fora = memberFactory.save(b -> b.team(lotte).nickname("fora"));
        Member pobi = memberFactory.save(builder -> builder.team(kia).nickname("pobi"));

        LocalDate d1 = LocalDate.of(2025, 1, 10);
        LocalDate d2 = LocalDate.of(2025, 2, 10);

        Game g1 = gameFactory.save(builder -> builder
                .stadium(stadiumGocheok)
                .date(d1)
                .homeTeam(lotte).awayTeam(kia)
                .homeScore(3).awayScore(1)
                .homeScoreBoard(TestFixture.getHomeScoreBoard())
                .awayScoreBoard(TestFixture.getAwayScoreBoard())
                .gameState(GameState.COMPLETED)
        );

        Game g2 = gameFactory.save(builder -> builder
                .stadium(stadiumJamsil)
                .date(d2)
                .homeTeam(kia).awayTeam(lotte)
                .homeScore(2).awayScore(4)
                .homeScoreBoard(TestFixture.getHomeScoreBoard())
                .awayScoreBoard(TestFixture.getAwayScoreBoard())
                .gameState(GameState.COMPLETED)
        );

        // fora: 2회 체크인(2경기), pobi: 1회 체크인(1경기)
        checkInFactory.save(bld -> bld.game(g1).member(fora).team(lotte));
        checkInFactory.save(bld -> bld.game(g2).member(fora).team(lotte));
        checkInFactory.save(bld -> bld.game(g1).member(pobi).team(kia));

        String token = authFactory.getAccessTokenByMemberId(fora.getId(), Role.USER);

        // when
        LeaderboardResponse[] array = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, token)
                .queryParam("limit", 1)
                .when().get("/api/v1/leaderboards")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(LeaderboardResponse[].class);

        List<LeaderboardResponse> responses = Arrays.asList(array);

        // then: 모든 타입이 포함되어 있고, MOST_CHECK_IN 1위는 fora
        assertThat(responses).hasSize(LeaderboardType.values().length);

        Map<LeaderboardType, LeaderboardResponse> byType = responses.stream()
                .collect(toMap(LeaderboardResponse::type, r -> r));

        LeaderboardResponse mostCheckIn = byType.get(LeaderboardType.MOST_CHECK_IN);
        assertThat(mostCheckIn).isNotNull();
        assertThat(mostCheckIn.items()).hasSize(1);

        LeaderboardItemResponse top = mostCheckIn.items().get(0);
        assertThat(top.rank()).isEqualTo(1);
        assertThat(top.memberId()).isEqualTo(fora.getId());
        assertThat(top.nickname()).isEqualTo("fora");
        assertThat(top.favoriteTeam()).isEqualTo(lotte.getShortName());
        assertThat(top.profileImageUrl()).isEqualTo(fora.getImageUrl());
        assertThat(top.score()).isEqualTo(2.0);
    }
}
