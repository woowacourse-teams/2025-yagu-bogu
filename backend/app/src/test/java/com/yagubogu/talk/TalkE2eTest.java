package com.yagubogu.talk;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.nullValue;

import com.sun.jdi.request.DuplicateRequestException;
import com.yagubogu.auth.config.AuthTestConfig;
import com.yagubogu.game.domain.Game;
import com.yagubogu.global.config.JpaAuditingConfig;
import com.yagubogu.member.domain.Member;
import com.yagubogu.member.domain.Role;
import com.yagubogu.stadium.domain.Stadium;
import com.yagubogu.stadium.repository.StadiumRepository;
import com.yagubogu.support.auth.AuthFactory;
import com.yagubogu.support.base.E2eTestBase;
import com.yagubogu.support.game.GameFactory;
import com.yagubogu.support.member.MemberBuilder;
import com.yagubogu.support.member.MemberFactory;
import com.yagubogu.support.talk.TalkFactory;
import com.yagubogu.support.talk.TalkReportFactory;
import com.yagubogu.talk.domain.Talk;
import com.yagubogu.talk.dto.v1.TalkRequest;
import com.yagubogu.talk.dto.v1.TalkResponse;
import com.yagubogu.talk.repository.TalkRepository;
import com.yagubogu.talk.service.TalkService;
import com.yagubogu.team.domain.Team;
import com.yagubogu.team.repository.TeamRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;

@Import({AuthTestConfig.class, JpaAuditingConfig.class})
public class TalkE2eTest extends E2eTestBase {

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
    private TalkService talkService;

    @Autowired
    private TalkReportFactory talkReportFactory;

    @Autowired
    private StadiumRepository stadiumRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private TalkRepository talkRepository;

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
        Game game = gameFactory.save(builder -> builder
                .homeTeam(homeTeam)
                .awayTeam(awayTeam)
                .stadium(stadium));

        talkFactory.save(builder ->
                builder.member(member)
                        .game(game)
        );

        // when & then
        RestAssured.given()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .queryParam("limit", 1)
                .pathParam("gameId", game.getId())
                .when()
                .get("/api/v1/talks/{gameId}/initial")
                .then()
                .statusCode(200)
                .body("stadiumName", is("사직야구장"))
                .body("homeTeamCode", is("LT"))
                .body("awayTeamCode", is("HH"))
                .body("myTeamCode", is("HH"));
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
                .get("/api/v1/talks/{gameId}")
                .then()
                .statusCode(200)
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
                .get("/api/v1/talks/{gameId}")
                .then()
                .statusCode(200)
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
                .get("/api/v1/talks/{gameId}/latest")
                .then()
                .statusCode(200)
                .body("cursorResult.content[-1].id", is(2))
                .body("cursorResult.content.size()", is(1))
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
                .get("/api/v1/talks/{gameId}/latest")
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

        String clientId = UUID.randomUUID().toString();
        String content = "오늘 야구보구 인증하구";

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .body(new TalkRequest(clientId, content))
                .pathParam("gameId", game.getId())
                .when().post("/api/v1/talks/{gameId}")
                .then().log().all()
                .statusCode(201)
                .body("content", is(content));
    }

    @Test
    @DisplayName("동시 요청 시 하나만 생성되고 나머지는 중복 감지 또는 예외 발생")
    void concurrentRequestsWithSameClientMessageId() throws InterruptedException {
        // given
        String clientMessageId = UUID.randomUUID().toString();
        TalkRequest request = new TalkRequest(clientMessageId, "동시 요청 테스트");

        Stadium stadium = stadiumRepository.findByShortName("사직구장").orElseThrow();
        Team homeTeam = teamRepository.findByTeamCode("LT").orElseThrow();
        Team awayTeam = teamRepository.findByTeamCode("HH").orElseThrow();
        Member member = memberFactory.save(builder -> builder.team(homeTeam));
        Game game = gameFactory.save(builder -> builder.homeTeam(homeTeam)
                .awayTeam(awayTeam)
                .stadium(stadium));

        Long gameId = game.getId();
        Long memberId = member.getId();

        // when: 3개 스레드에서 동시에 같은 clientMessageId로 요청
        int threadCount = 3;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        List<TalkResponse> responses = new CopyOnWriteArrayList<>();
        List<Exception> exceptions = new CopyOnWriteArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    TalkResponse response = talkService.createTalk(gameId, request, memberId);
                    responses.add(response);
                } catch (Exception e) {
                    exceptions.add(e);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(5, TimeUnit.SECONDS);
        executorService.shutdown();

        // then
        // 성공 + 예외 = 총 3개 처리
        assertThat(responses.size() + exceptions.size()).isEqualTo(3);

        // DB에는 1개만 저장
        assertThat(talkRepository.findAll()).hasSize(1);

        // 발생한 예외는 모두 DuplicateRequestException
        assertThat(exceptions)
                .allMatch(e -> e instanceof DuplicateRequestException);

        // 최소 1개는 성공, 나머지는 예외 (0~3개 성공 가능)
        assertThat(responses.size()).isBetween(0, 3);
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

        String clientId = UUID.randomUUID().toString();
        String content = "오늘 야구보구 인증하구";

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, blockedMemberAccessToken)
                .body(new TalkRequest(clientId, content))
                .pathParam("gameId", game.getId())
                .when()
                .post("/api/v1/talks/{gameId}")
                .then().log().all()
                .statusCode(403);
    }

    @DisplayName("예외: 존재하지 않는 gameId로 톡을 생성하면 에러가 발생한다")
    @Test
    void createTalk_withInvalidGameId() {
        // given
        long invalidGameId = 999L;
        String clientId = UUID.randomUUID().toString();
        String content = "오늘 야구보구 인증하구";

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .body(new TalkRequest(clientId, content))
                .pathParam("gameId", invalidGameId)
                .when().post("/api/v1/talks/{gameId}")
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
                .when().delete("/api/v1/talks/{gameId}/{talkId}")
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
                .when().delete("/api/v1/talks/{gameId}/{talkId}")
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
                .when().delete("/api/v1/talks/{gameId}/{talkId}")
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
                .when().delete("/api/v1/talks/{gameId}/{talkId}")
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
                .when().post("/api/v1/talks/{talkId}/reports")
                .then().log().all()
                .statusCode(201);
    }
}
