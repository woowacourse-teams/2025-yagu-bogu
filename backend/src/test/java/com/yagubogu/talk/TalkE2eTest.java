package com.yagubogu.talk;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.nullValue;

import com.yagubogu.auth.config.AuthTestConfig;
import com.yagubogu.game.domain.Game;
import com.yagubogu.member.domain.Member;
import com.yagubogu.member.domain.Role;
import com.yagubogu.stadium.domain.Stadium;
import com.yagubogu.stadium.repository.StadiumRepository;
import com.yagubogu.support.auth.AuthFactory;
import com.yagubogu.support.game.GameFactory;
import com.yagubogu.support.member.MemberBuilder;
import com.yagubogu.support.member.MemberFactory;
import com.yagubogu.support.talk.TalkFactory;
import com.yagubogu.support.talk.TalkReportFactory;
import com.yagubogu.talk.domain.Talk;
import com.yagubogu.talk.dto.TalkRequest;
import com.yagubogu.team.domain.Team;
import com.yagubogu.team.repository.TeamRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

@Import(AuthTestConfig.class)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class TalkE2eTest {

    @LocalServerPort
    private int port;

    @Autowired
    private AuthFactory authFactory;

    @Autowired
    private MemberFactory memberFactory;

    @Autowired
    private GameFactory gameFactory;

    @Autowired
    private TalkFactory talkFactory;

    @Autowired
    private TalkReportFactory talkReportFactory;

    @Autowired
    private StadiumRepository stadiumRepository;

    @Autowired
    private TeamRepository teamRepository;

    private String accessToken;

    private Member member;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        Team favoriteTeam = teamRepository.findByTeamCode("HH").orElseThrow();
        member = memberFactory.save(builder -> builder.team(favoriteTeam));
        accessToken = authFactory.getAccessTokenByMemberId(member.getId(), Role.USER);
    }

    @DisplayName("톡의 첫 페이지를 조회한다")
    @Test
    void findTalks_firstPage() {
        // given
        Stadium stadium = stadiumRepository.findByShortName("사직구장").orElseThrow();
        Team homeTeam = teamRepository.findByTeamCode("LT").orElseThrow();
        Team awayTeam = teamRepository.findByTeamCode("HH").orElseThrow();
        Game game = gameFactory.save(builder -> builder.homeTeam(homeTeam)
                .awayTeam(awayTeam)
                .stadium(stadium));

        Member other = memberFactory.save(builder -> builder.team(homeTeam));
        talkFactory.save(builder ->
                builder.member(other)
                        .game(game)
        );

        // when & then
        RestAssured.given()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .queryParam("limit", 1)
                .pathParam("gameId", game.getId())
                .when()
                .get("/api/talks/{gameId}")
                .then()
                .statusCode(200)
                .body("stadiumName", is("사직야구장"))
                .body("homeTeamName", is("롯데"))
                .body("awayTeamName", is("한화"))
                .body("cursorResult.content[0].id", is(1));
    }

    @DisplayName("톡의 중간 페이지를 조회한다")
    @Test
    void findTalks_middlePage() {
        // given
        Stadium stadium = stadiumRepository.findByShortName("사직구장").orElseThrow();
        Team homeTeam = teamRepository.findByTeamCode("LT").orElseThrow();
        Team awayTeam = teamRepository.findByTeamCode("HH").orElseThrow();
        Game game = gameFactory.save(builder -> builder.homeTeam(homeTeam)
                .awayTeam(awayTeam)
                .stadium(stadium));

        Member other = memberFactory.save(builder -> builder.team(homeTeam));
        talkFactory.save(builder ->
                builder.member(other)
                        .game(game)
        );
        talkFactory.save(builder ->
                builder.member(other)
                        .game(game)
        );
        talkFactory.save(builder ->
                builder.member(other)
                        .game(game)
        );

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .queryParam("before", 3)
                .queryParam("limit", 1)
                .pathParam("gameId", game.getId())
                .when()
                .get("/api/talks/{gameId}")
                .then()
                .statusCode(200)
                .body("stadiumName", is("사직야구장"))
                .body("homeTeamName", is("롯데"))
                .body("awayTeamName", is("한화"))
                .body("cursorResult.content[0].id", is(2))
                .body("cursorResult.nextCursorId", is(2));
    }

    @DisplayName("톡의 마지막 페이지를 조회한다")
    @Test
    void findTalks_lastPage() {
        // given
        Stadium stadium = stadiumRepository.findByShortName("사직구장").orElseThrow();
        Team homeTeam = teamRepository.findByTeamCode("LT").orElseThrow();
        Team awayTeam = teamRepository.findByTeamCode("HH").orElseThrow();
        Game game = gameFactory.save(builder -> builder.homeTeam(homeTeam)
                .awayTeam(awayTeam)
                .stadium(stadium));

        Member other = memberFactory.save(builder -> builder.team(homeTeam));
        talkFactory.save(builder ->
                builder.member(other)
                        .game(game)
        );
        talkFactory.save(builder ->
                builder.member(other)
                        .game(game)
        );
        talkFactory.save(builder ->
                builder.member(other)
                        .game(game)
        );

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .queryParam("before", 2)
                .queryParam("limit", 1)
                .pathParam("gameId", game.getId())
                .when()
                .get("/api/talks/{gameId}")
                .then()
                .statusCode(200)
                .body("stadiumName", is("사직야구장"))
                .body("homeTeamName", is("롯데"))
                .body("awayTeamName", is("한화"))
                .body("cursorResult.content[0].id", is(1))
                .body("cursorResult.nextCursorId", is(nullValue()));
    }

    @DisplayName("새 톡을 가져온다")
    @Test
    void findNewTalks_existing() {
        // given
        Stadium stadium = stadiumRepository.findByShortName("사직구장").orElseThrow();
        Team homeTeam = teamRepository.findByTeamCode("LT").orElseThrow();
        Team awayTeam = teamRepository.findByTeamCode("HH").orElseThrow();
        Game game = gameFactory.save(builder -> builder.homeTeam(homeTeam)
                .awayTeam(awayTeam)
                .stadium(stadium));

        Member other = memberFactory.save(builder -> builder.team(homeTeam));
        talkFactory.save(builder ->
                builder.member(other)
                        .game(game)
        );
        talkFactory.save(builder ->
                builder.member(other)
                        .game(game)
        );

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .queryParam("after", 1)
                .queryParam("limit", 1)
                .pathParam("gameId", game.getId())
                .when()
                .get("/api/talks/{gameId}/latest")
                .then()
                .statusCode(200)
                .body("cursorResult.content.size()", is(1))
                .body("cursorResult.content[-1].id", is(2))
                .body("cursorResult.nextCursorId", is(2));
    }

    @DisplayName("새 톡이 없다면 가져오지 않는다")
    @Test
    void findNewTalks_noExisting() {
        // given
        Stadium stadium = stadiumRepository.findByShortName("사직구장").orElseThrow();
        Team homeTeam = teamRepository.findByTeamCode("LT").orElseThrow();
        Team awayTeam = teamRepository.findByTeamCode("HH").orElseThrow();
        Game game = gameFactory.save(builder -> builder.homeTeam(homeTeam)
                .awayTeam(awayTeam)
                .stadium(stadium));

        Member other = memberFactory.save(builder -> builder.team(homeTeam));
        talkFactory.save(builder ->
                builder.member(other)
                        .game(game)
        );
        talkFactory.save(builder ->
                builder.member(other)
                        .game(game)
        );

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .queryParam("after", 2)
                .queryParam("limit", 1)
                .pathParam("gameId", game.getId())
                .when()
                .get("/api/talks/{gameId}/latest")
                .then()
                .statusCode(200)
                .body("cursorResult.content.size()", is(0))
                .body("cursorResult.nextCursorId", is(2));
    }

    @DisplayName("정상적으로 톡을 저장하고 응답을 반환한다")
    @Test
    void createTalk() {
        // given
        Stadium stadium = stadiumRepository.findByShortName("사직구장").orElseThrow();
        Team homeTeam = teamRepository.findByTeamCode("LT").orElseThrow();
        Team awayTeam = teamRepository.findByTeamCode("HH").orElseThrow();
        Game game = gameFactory.save(builder -> builder.homeTeam(homeTeam)
                .awayTeam(awayTeam)
                .stadium(stadium));

        String content = "오늘 야구보구 인증하구";

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .body(new TalkRequest(content))
                .pathParam("gameId", game.getId())
                .when().post("/api/talks/{gameId}")
                .then().log().all()
                .statusCode(201)
                .body("content", is(content));
    }

    @DisplayName("예외: 신고를 기준보다 많이 받은 사용자는 톡을 생성할 수 없다")
    @Test
    void createTalk_blockedFromStadium() {
        // given
        Stadium stadium = stadiumRepository.findByShortName("사직구장").orElseThrow();
        Team homeTeam = teamRepository.findByTeamCode("LT").orElseThrow();
        Team awayTeam = teamRepository.findByTeamCode("HH").orElseThrow();
        Game game = gameFactory.save(builder -> builder.homeTeam(homeTeam)
                .awayTeam(awayTeam)
                .stadium(stadium));

        Member kindMember2 = memberFactory.save(MemberBuilder::build);
        Member kindMember3 = memberFactory.save(MemberBuilder::build);
        Member kindMember4 = memberFactory.save(MemberBuilder::build);
        Member kindMember5 = memberFactory.save(MemberBuilder::build);
        Member kindMember6 = memberFactory.save(MemberBuilder::build);
        Member kindMember7 = memberFactory.save(MemberBuilder::build);
        Member kindMember8 = memberFactory.save(MemberBuilder::build);
        Member kindMember9 = memberFactory.save(MemberBuilder::build);
        Member kindMember10 = memberFactory.save(MemberBuilder::build);

        Member blockedMember = memberFactory.save(builder -> builder.team(homeTeam));
        String blockedMemberAccessToken = authFactory.getAccessTokenByMemberId(blockedMember.getId(), Role.USER);
        Talk blockedTalk = talkFactory.save(builder ->
                builder.member(blockedMember)
                        .game(game)
        );
        talkReportFactory.save(builder -> builder.talk(blockedTalk)
                .talk(blockedTalk)
                .reporter(member)
        );
        talkReportFactory.save(builder -> builder.talk(blockedTalk)
                .talk(blockedTalk)
                .reporter(kindMember2)
        );
        talkReportFactory.save(builder -> builder.talk(blockedTalk)
                .talk(blockedTalk)
                .reporter(kindMember3)
        );
        talkReportFactory.save(builder -> builder.talk(blockedTalk)
                .talk(blockedTalk)
                .reporter(kindMember4)
        );
        talkReportFactory.save(builder -> builder.talk(blockedTalk)
                .talk(blockedTalk)
                .reporter(kindMember5)
        );
        talkReportFactory.save(builder -> builder.talk(blockedTalk)
                .talk(blockedTalk)
                .reporter(kindMember6)
        );
        talkReportFactory.save(builder -> builder.talk(blockedTalk)
                .talk(blockedTalk)
                .reporter(kindMember7)
        );
        talkReportFactory.save(builder -> builder.talk(blockedTalk)
                .talk(blockedTalk)
                .reporter(kindMember8)
        );
        talkReportFactory.save(builder -> builder.talk(blockedTalk)
                .talk(blockedTalk)
                .reporter(kindMember9)
        );
        talkReportFactory.save(builder -> builder.talk(blockedTalk)
                .talk(blockedTalk)
                .reporter(kindMember10)
        );

        String content = "오늘 야구보구 인증하구";

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, blockedMemberAccessToken)
                .body(new TalkRequest(content))
                .pathParam("gameId", game.getId())
                .when()
                .post("/api/talks/{gameId}")
                .then().log().all()
                .statusCode(403);
    }

    @DisplayName("예외: 존재하지 않는 gameId로 톡을 생성하면 에러가 발생한다")
    @Test
    void createTalk_withInvalidGameId() {
        // given
        long invalidGameId = 999L;
        String content = "오늘 야구보구 인증하구";

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .body(new TalkRequest(content))
                .pathParam("gameId", invalidGameId)
                .when().post("/api/talks/{gameId}")
                .then().log().all()
                .statusCode(404);
    }

    @DisplayName("톡을 삭제한다")
    @Test
    void removeTalk() {
        // given
        Stadium stadium = stadiumRepository.findByShortName("사직구장").orElseThrow();
        Team homeTeam = teamRepository.findByTeamCode("LT").orElseThrow();
        Team awayTeam = teamRepository.findByTeamCode("HH").orElseThrow();
        Game game = gameFactory.save(builder -> builder.homeTeam(homeTeam)
                .awayTeam(awayTeam)
                .stadium(stadium));

        Talk myTalk = talkFactory.save(builder -> builder.game(game)
                .member(member));

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .pathParam("gameId", game.getId())
                .pathParam("talkId", myTalk.getId())
                .when().delete("/api/talks/{gameId}/{talkId}")
                .then().log().all()
                .statusCode(204);
    }

    @DisplayName("예외: 존재하지 않는 talkId로 톡을 삭제하면 예외가 발생한다")
    @Test
    void removeTalk_withInvalidTalkId() {
        // given
        Stadium stadium = stadiumRepository.findByShortName("사직구장").orElseThrow();
        Team homeTeam = teamRepository.findByTeamCode("LT").orElseThrow();
        Team awayTeam = teamRepository.findByTeamCode("HH").orElseThrow();
        Game game = gameFactory.save(builder -> builder.homeTeam(homeTeam)
                .awayTeam(awayTeam)
                .stadium(stadium));

        long invalidTalkId = 999L;

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .pathParam("gameId", game.getId())
                .pathParam("talkId", invalidTalkId)
                .when().delete("/api/talks/{gameId}/{talkId}")
                .then().log().all()
                .statusCode(404);
    }

    @DisplayName("예외: talk의 gameId와 요청 gameId가 일치하지 않으면 예외가 발생한다")
    @Test
    void removeTalk_withMismatchedGameId() {
        // given
        Stadium stadium = stadiumRepository.findByShortName("사직구장").orElseThrow();
        Team homeTeam = teamRepository.findByTeamCode("LT").orElseThrow();
        Team awayTeam = teamRepository.findByTeamCode("HH").orElseThrow();
        Game game = gameFactory.save(builder -> builder.homeTeam(homeTeam)
                .awayTeam(awayTeam)
                .stadium(stadium));
        Game invalidGame = gameFactory.save(builder -> builder.homeTeam(homeTeam)
                .awayTeam(awayTeam)
                .stadium(stadium));

        Talk myTalk = talkFactory.save(builder -> builder.member(member)
                .game(game));

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .pathParam("gameId", invalidGame.getId())
                .pathParam("talkId", myTalk.getId())
                .when().delete("/api/talks/{gameId}/{talkId}")
                .then().log().all()
                .statusCode(400);
    }

    @DisplayName("예외: talk의 memberId와 요청 memberId가 일치하지 않으면 예외가 발생한다")
    @Test
    void removeTalk_withMismatchedMemberId() {
        // given
        Stadium stadium = stadiumRepository.findByShortName("사직구장").orElseThrow();
        Team homeTeam = teamRepository.findByTeamCode("LT").orElseThrow();
        Team awayTeam = teamRepository.findByTeamCode("HH").orElseThrow();
        Game game = gameFactory.save(builder -> builder.homeTeam(homeTeam)
                .awayTeam(awayTeam)
                .stadium(stadium));
        Talk myTalk = talkFactory.save(builder -> builder.member(member)
                .game(game));
        Member invalidMember = memberFactory.save(builder -> builder.team(homeTeam));
        String accessTokenByInvalidMember = authFactory.getAccessTokenByMemberId(invalidMember.getId(), Role.USER);

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessTokenByInvalidMember)
                .pathParam("gameId", game.getId())
                .pathParam("talkId", myTalk.getId())
                .when().delete("/api/talks/{gameId}/{talkId}")
                .then().log().all()
                .statusCode(403);
    }

    @DisplayName("톡을 신고한다")
    @Test
    void reportTalk() {
        // given
        Stadium stadium = stadiumRepository.findByShortName("사직구장").orElseThrow();
        Team homeTeam = teamRepository.findByTeamCode("LT").orElseThrow();
        Team awayTeam = teamRepository.findByTeamCode("HH").orElseThrow();
        Game game = gameFactory.save(builder -> builder.homeTeam(homeTeam)
                .awayTeam(awayTeam)
                .stadium(stadium));
        Member invalidMember = memberFactory.save(builder -> builder.team(homeTeam));
        Talk reportedTalk = talkFactory.save(builder -> builder.member(invalidMember)
                .game(game));

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .pathParam("talkId", reportedTalk.getId())
                .when().post("/api/talks/{talkId}/reports")
                .then().log().all()
                .statusCode(201);
    }
}
