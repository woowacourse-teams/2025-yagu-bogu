package com.yagubogu.ticket;

import static org.assertj.core.api.Assertions.assertThat;

import com.yagubogu.auth.config.AuthTestConfig;
import com.yagubogu.checkin.domain.CheckIn;
import com.yagubogu.game.domain.Game;
import com.yagubogu.game.domain.GameState;
import com.yagubogu.global.config.JpaAuditingConfig;
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
import com.yagubogu.ticket.dto.v1.TicketResponse;
import io.restassured.RestAssured;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;

@Import({AuthTestConfig.class, JpaAuditingConfig.class})
class TicketE2eTest extends E2eTestBase {

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

    private Team lg, doosan;
    private Stadium stadiumJamsil;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;

        lg = teamRepository.findByTeamCode("LG").orElseThrow();
        doosan = teamRepository.findByTeamCode("OB").orElseThrow();
        stadiumJamsil = stadiumRepository.findById(2L).orElseThrow();
    }

    @DisplayName("직관 티켓 화면 정보를 조회한다")
    @Test
    void findTicket() {
        // given
        Member member = memberFactory.save(builder -> builder.team(lg));
        String accessToken = authFactory.getAccessTokenByMemberId(member.getId(), Role.USER);

        saveCheckIn(member, lg, 5, doosan, 1, LocalDate.of(2026, 4, 1));
        saveCheckIn(member, doosan, 2, lg, 4, LocalDate.of(2026, 4, 15));
        Game targetGame = saveGame(lg, 2, doosan, 3, LocalDate.of(2026, 5, 7));
        CheckIn targetCheckIn = checkInFactory.save(builder -> builder
                .member(member)
                .team(lg)
                .game(targetGame)
        );

        // when
        TicketResponse actual = RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .when().get("/api/v1/tickets/{checkInId}", targetCheckIn.getId())
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(TicketResponse.class);

        // then
        assertThat(actual.homeTeamCode()).isEqualTo("LG");
        assertThat(actual.awayTeamCode()).isEqualTo("OB");
        assertThat(actual.homeScore()).isEqualTo(2);
        assertThat(actual.awayScore()).isEqualTo(3);
        assertThat(actual.myTeamCode()).isEqualTo("LG");
        assertThat(actual.stadiumName()).isEqualTo("잠실 야구장");
        assertThat(actual.attendanceDate()).isEqualTo(LocalDate.of(2026, 5, 7));
        assertThat(actual.record().year()).isEqualTo(2026);
        assertThat(actual.record().winCounts()).isEqualTo(2);
        assertThat(actual.record().drawCounts()).isZero();
        assertThat(actual.record().loseCounts()).isEqualTo(1);
        assertThat(actual.record().winRate()).isEqualTo(66.7);
        assertThat(actual.record().checkInCounts()).isEqualTo(3);
    }

    private void saveCheckIn(
            final Member member,
            final Team homeTeam,
            final int homeScore,
            final Team awayTeam,
            final int awayScore,
            final LocalDate date
    ) {
        Game game = saveGame(homeTeam, homeScore, awayTeam, awayScore, date);
        checkInFactory.save(builder -> builder
                .member(member)
                .team(lg)
                .game(game)
        );
    }

    private Game saveGame(
            final Team homeTeam,
            final int homeScore,
            final Team awayTeam,
            final int awayScore,
            final LocalDate date
    ) {
        return gameFactory.save(builder -> builder
                .stadium(stadiumJamsil)
                .homeTeam(homeTeam)
                .awayTeam(awayTeam)
                .homeScore(homeScore)
                .awayScore(awayScore)
                .homeScoreBoard(TestFixture.getHomeScoreBoardAbout(homeScore))
                .awayScoreBoard(TestFixture.getAwayScoreBoardAbout(awayScore))
                .date(date)
                .gameState(GameState.COMPLETED)
        );
    }
}
