package com.yagubogu.like;

import static org.hamcrest.CoreMatchers.is;

import com.yagubogu.auth.config.AuthTestConfig;
import com.yagubogu.game.domain.Game;
import com.yagubogu.global.config.JpaAuditingConfig;
import com.yagubogu.like.dto.LikeBatchRequest;
import com.yagubogu.like.dto.LikeBatchRequest.LikeDelta;
import com.yagubogu.member.domain.Member;
import com.yagubogu.member.domain.Role;
import com.yagubogu.stadium.domain.Stadium;
import com.yagubogu.stadium.repository.StadiumRepository;
import com.yagubogu.support.E2eTestBase;
import com.yagubogu.support.auth.AuthFactory;
import com.yagubogu.support.game.GameFactory;
import com.yagubogu.support.member.MemberFactory;
import com.yagubogu.team.domain.Team;
import com.yagubogu.team.repository.TeamRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;

@Import({AuthTestConfig.class, JpaAuditingConfig.class})
public class LikeE2eTest extends E2eTestBase {

    @LocalServerPort
    private int port;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private StadiumRepository stadiumRepository;

    @Autowired
    private GameFactory gameFactory;

    @Autowired
    private MemberFactory memberFactory;

    @Autowired
    private AuthFactory authFactory;

    private String accessToken;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @DisplayName("좋아요 카운트: 초기값은 비어있다")
    @Test
    void findLikeCounts_initiallyEmpty() {
        // given
        Stadium stadium = stadiumRepository.findByShortName("사직구장").orElseThrow();
        Team homeTeam = teamRepository.findByTeamCode("LT").orElseThrow();
        Team awayTeam = teamRepository.findByTeamCode("HH").orElseThrow();
        Game game = gameFactory.save(builder -> builder
                .homeTeam(homeTeam)
                .awayTeam(awayTeam)
                .stadium(stadium));
        Member member = memberFactory.save(b -> b.team(homeTeam));
        accessToken = authFactory.getAccessTokenByMemberId(member.getId(), Role.USER);

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .pathParam("gameId", game.getId())
                .when()
                .get("/api/games/{gameId}/likes/counts")
                .then().log().all()
                .statusCode(200)
                .body("gameId", is(game.getId().intValue()))
                .body("counts.size()", is(0));
    }

    @DisplayName("좋아요 배치를 적용하고 카운트를 반환한다")
    @Test
    void applyLikeBatch_andGetCounts() {
        // given
        Stadium stadium = stadiumRepository.findByShortName("사직구장").orElseThrow();
        Team homeTeam = teamRepository.findByTeamCode("LT").orElseThrow();
        Team awayTeam = teamRepository.findByTeamCode("HH").orElseThrow();
        Game game = gameFactory.save(builder -> builder
                .homeTeam(homeTeam)
                .awayTeam(awayTeam)
                .stadium(stadium));
        Member member = memberFactory.save(b -> b.team(homeTeam));
        accessToken = authFactory.getAccessTokenByMemberId(member.getId(), Role.USER);

        LikeBatchRequest request = new LikeBatchRequest(
                "test-client-1",
                1L,
                new LikeDelta(homeTeam.getId(), 3)
        );

        // when & then
        RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .contentType(ContentType.JSON)
                .pathParam("gameId", game.getId())
                .body(request)
                .when()
                .post("/api/games/{gameId}/likes/batch")
                .then().log().all()
                .statusCode(200)
                .body("gameId", is(game.getId().intValue()))
                .body("counts.find {it.teamId == %s }.totalCount".formatted(homeTeam.getId()), is(3));
    }
}

