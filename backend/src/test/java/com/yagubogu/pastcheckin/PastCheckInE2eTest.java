package com.yagubogu.pastcheckin;

import static org.assertj.core.api.Assertions.assertThat;

import com.yagubogu.auth.config.AuthTestConfig;
import com.yagubogu.checkin.dto.CreatePastCheckInRequest;
import com.yagubogu.checkin.repository.CheckInRepository;
import com.yagubogu.game.domain.Game;
import com.yagubogu.game.domain.GameState;
import com.yagubogu.global.config.JpaAuditingConfig;
import com.yagubogu.member.domain.Member;
import com.yagubogu.member.domain.Role;
import com.yagubogu.stadium.domain.Stadium;
import com.yagubogu.stadium.repository.StadiumRepository;
import com.yagubogu.support.E2eTestBase;
import com.yagubogu.support.TestFixture;
import com.yagubogu.support.auth.AuthFactory;
import com.yagubogu.support.checkin.CheckInFactory;
import com.yagubogu.support.game.GameFactory;
import com.yagubogu.support.member.MemberFactory;
import com.yagubogu.team.domain.Team;
import com.yagubogu.team.repository.TeamRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;

@Import({AuthTestConfig.class, JpaAuditingConfig.class})
public class PastCheckInE2eTest extends E2eTestBase {

    @LocalServerPort
    private int port;

    @Autowired
    private AuthFactory authFactory;

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
    private CheckInRepository checkInRepository;

    private Team kia, lotte;
    private Stadium stadiumGocheok;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        kia = teamRepository.findByTeamCode("HT").orElseThrow();
        lotte = teamRepository.findByTeamCode("LT").orElseThrow();
        stadiumGocheok = stadiumRepository.findById(3L).orElseThrow();
    }

    @DisplayName("PastCheckIn을 저장한다")
    @Test
    void createPastCheckIn() {
        // given
        Member mint = memberFactory.save(b -> b.team(lotte));
        String accessToken = authFactory.getAccessTokenByMemberId(mint.getId(), Role.USER);

        LocalDate date = LocalDate.of(2025, 1, 1);

        Game game = gameFactory.save(builder ->
                builder.stadium(stadiumGocheok)
                        .date(date)
                        .homeTeam(lotte).homeScore(5).homeScoreBoard(TestFixture.getHomeScoreBoard())
                        .awayTeam(kia).awayScore(3).awayScoreBoard(TestFixture.getAwayScoreBoard())
                        .gameState(GameState.COMPLETED)
        );

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .body(new CreatePastCheckInRequest(game.getId(), date))
                .when().post("/api/past-check-ins")
                .then().log().all()
                .statusCode(201);

        boolean exists = checkInRepository.existsByMemberAndGameDate(mint, date);
        assertThat(exists).isTrue();
    }

    @DisplayName("예외: 동일 날짜에 이미 CheckIn이 존재하면 400 발생한다")
    @Test
    void createPastCheckIn_fail_whenCheckInExists() {
        // given
        Member member = memberFactory.save(b -> b.team(lotte));
        String accessToken = authFactory.getAccessTokenByMemberId(member.getId(), Role.USER);

        LocalDate date = LocalDate.of(2025, 2, 2);

        // 게임 생성
        Game game = gameFactory.save(builder ->
                builder.stadium(stadiumGocheok)
                        .date(date)
                        .homeTeam(lotte).homeScore(4).homeScoreBoard(TestFixture.getHomeScoreBoard())
                        .awayTeam(kia).awayScore(1).awayScoreBoard(TestFixture.getAwayScoreBoard())
                        .gameState(GameState.COMPLETED)
        );

        checkInFactory.save(b -> b.team(lotte).member(member).game(game));

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .body(new CreatePastCheckInRequest(game.getId(), date))
                .when().post("/api/past-check-ins")
                .then().log().all()
                .statusCode(409);
    }

    @DisplayName("예외: 동일 날짜에 이미 PastCheckIn이 존재하면 400 발생한다")
    @Test
    void createPastCheckIn_fail_whenPastCheckInExists() {
        // given
        Member member = memberFactory.save(b -> b.team(lotte));
        String accessToken = authFactory.getAccessTokenByMemberId(member.getId(), Role.USER);

        LocalDate date = LocalDate.of(2025, 3, 3);

        Game game = gameFactory.save(builder ->
                builder.stadium(stadiumGocheok)
                        .date(date)
                        .homeTeam(lotte).homeScore(6).homeScoreBoard(TestFixture.getHomeScoreBoard())
                        .awayTeam(kia).awayScore(2).awayScoreBoard(TestFixture.getAwayScoreBoard())
                        .gameState(GameState.COMPLETED)
        );

        checkInFactory.save(b -> b.game(game).member(member).team(lotte));

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .body(new CreatePastCheckInRequest(game.getId(), date))
                .when().post("/api/past-check-ins")
                .then().log().all()
                .statusCode(409);
    }
}
