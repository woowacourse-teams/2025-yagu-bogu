package com.yagubogu.member;

import com.yagubogu.auth.config.AuthTestConfig;
import com.yagubogu.badge.domain.Badge;
import com.yagubogu.badge.domain.Policy;
import com.yagubogu.badge.repository.BadgeRepository;
import com.yagubogu.game.domain.Game;
import com.yagubogu.game.domain.GameState;
import com.yagubogu.global.config.JpaAuditingConfig;
import com.yagubogu.member.domain.Member;
import com.yagubogu.member.domain.Role;
import com.yagubogu.member.dto.v1.MemberFavoriteRequest;
import com.yagubogu.member.dto.v1.MemberFavoriteResponse;
import com.yagubogu.member.dto.v1.MemberInfoResponse;
import com.yagubogu.member.dto.v1.MemberNicknameRequest;
import com.yagubogu.member.dto.v1.MemberNicknameResponse;
import com.yagubogu.member.dto.v1.MemberProfileResponse;
import com.yagubogu.stadium.domain.Stadium;
import com.yagubogu.stadium.repository.StadiumRepository;
import com.yagubogu.support.auth.AuthFactory;
import com.yagubogu.support.badge.MemberBadgeFactory;
import com.yagubogu.support.base.E2eTestBase;
import com.yagubogu.support.checkin.CheckInFactory;
import com.yagubogu.support.game.GameFactory;
import com.yagubogu.support.member.MemberBuilder;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

@Import({AuthTestConfig.class, JpaAuditingConfig.class})
public class MemberE2eTest extends E2eTestBase {

    @LocalServerPort
    private int port;

    @Autowired
    private MemberFactory memberFactory;

    @Autowired
    private AuthFactory authFactory;

    @Autowired
    private GameFactory gameFactory;

    @Autowired
    private CheckInFactory checkInFactory;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private BadgeRepository badgeRepository;

    @Autowired
    private StadiumRepository stadiumRepository;

    @Autowired
    private MemberBadgeFactory memberBadgeFactory;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @DisplayName("멤버의 응원팀을 조회한다")
    @Test
    void findFavorites() {
        // given
        String teamCode = "HT";
        Team team = teamRepository.findByTeamCode(teamCode).orElseThrow();
        Member member = memberFactory.save(builder -> builder.team(team));
        String accessToken = authFactory.getAccessTokenByMemberId(member.getId(), Role.USER);

        // when
        MemberFavoriteResponse actual = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .when().get("/api/v1/members/favorites")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(MemberFavoriteResponse.class);

        // then
        assertThat(actual.favorite()).isEqualTo(member.getTeam().getShortName());
    }

    @DisplayName("멤버의 닉네임을 조회한다")
    @Test
    void findNickName() {
        // given
        String nickname = "user";
        Member member = memberFactory.save(builder -> builder.nickname(nickname));
        String accessToken = authFactory.getAccessTokenByMemberId(member.getId(), Role.USER);

        // when
        MemberNicknameResponse actual = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .when().get("/api/v1/members/me/nickname")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(MemberNicknameResponse.class);

        // then
        assertThat(actual.nickname()).isEqualTo(nickname);
    }

    @DisplayName("멤버의 닉네임을 수정한다")
    @Test
    void patchNickname() {
        // given
        String oldNickname = "두리";
        Member member = memberFactory.save(builder -> builder.nickname(oldNickname));
        String accessToken = authFactory.getAccessTokenByMemberId(member.getId(), Role.USER);
        String newNickname = "둘리";

        // when
        MemberNicknameResponse actual = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .body(new MemberNicknameRequest(newNickname))
                .when().patch("/api/v1/members/me/nickname")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(MemberNicknameResponse.class);

        // then
        assertThat(actual.nickname()).isEqualTo(newNickname);
    }

    @DisplayName("예외: 닉네임 수정 시 중복된 닉네임이면 409 상태를 반환한다")
    @Test
    void patchNickname_duplicateNicknameReturn409Status() {
        // given
        String existNickname = "존재하는닉네임";
        Member member1 = memberFactory.save(builder -> builder.nickname(existNickname));
        Member member2 = memberFactory.save(builder -> builder.nickname("우가"));
        String accessToken = authFactory.getAccessTokenByMemberId(member2.getId(), Role.USER);

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .body(new MemberNicknameRequest(existNickname))
                .when().patch("/api/v1/members/me/nickname")
                .then().log().all()
                .statusCode(409);
    }

    @DisplayName("예외: 닉네임 수정 시 길이 제한을 초과하면 422 상태를 반환한다")
    @Test
    void patchNickname_nickNameTooLongReturn422Status() {
        // given
        Member member2 = memberFactory.save(builder -> builder.nickname("우가"));
        String longNickName = "12345678901234567890123456";

        String accessToken = authFactory.getAccessTokenByMemberId(member2.getId(), Role.USER);

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .body(new MemberNicknameRequest(longNickName))
                .when().patch("/api/v1/members/me/nickname")
                .then().log().all()
                .statusCode(422);
    }

    @DisplayName("회원 탈퇴한다")
    @Test
    void removeMember() {
        // given
        Member member = memberFactory.save(MemberBuilder::build);
        String accessToken = authFactory.getAccessTokenByMemberId(member.getId(), Role.USER);

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .when().delete("/api/v1/members/me")
                .then().log().all()
                .statusCode(204);
    }

    @DisplayName("팀을 변경한다")
    @Test
    void updateTeam() {
        // given
        String teamCode = "HT";
        Team team = teamRepository.findByTeamCode(teamCode).orElseThrow();
        Member member = memberFactory.save(builder -> builder.team(team));
        String accessToken = authFactory.getAccessTokenByMemberId(member.getId(), Role.USER);

        String changedTeamCode = "SS";
        MemberFavoriteRequest request = new MemberFavoriteRequest(changedTeamCode);

        String changedTeamShortName = "삼성";

        // when
        MemberFavoriteResponse actual = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .body(request)
                .when().patch("/api/v1/members/favorites")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(MemberFavoriteResponse.class);

        // then
        assertThat(actual.favorite()).isEqualTo(changedTeamShortName);
    }

    @DisplayName("멤버의 정보를 조회한다")
    @Test
    void findMember() {
        // given
        Team team = teamRepository.findByTeamCode("HT").orElseThrow();
        Member member = memberFactory.save(builder ->
                builder.nickname("우가")
                        .team(team)
        );
        String accessToken = authFactory.getAccessTokenByMemberId(member.getId(), Role.USER);

        // when
        MemberInfoResponse actual = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .when().get("/api/v1/members/me")
                .then().log().all()
                .statusCode(200)
                .extract()
                .as(MemberInfoResponse.class);

        // then
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(actual.nickname()).isEqualTo(member.getNickname().getValue());
            softAssertions.assertThat(actual.profileImageUrl()).isEqualTo(member.getImageUrl());
            softAssertions.assertThat(actual.createdAt()).isEqualTo(member.getCreatedAt().toLocalDate());
            softAssertions.assertThat(actual.favoriteTeam()).isEqualTo(member.getTeam().getShortName());
        });
    }

    @DisplayName("뱃지를 조회한다")
    @Test
    void findBadges() {
        // given
        Member member = memberFactory.save(builder -> builder.nickname("우가"));
        String accessToken = authFactory.getAccessTokenByMemberId(member.getId(), Role.USER);

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .when().get("/api/v1/members/me/badges")
                .then().log().all()
                .statusCode(200);
    }

    @DisplayName("대표 뱃지를 수정한다")
    @Test
    void patchRepresentativeBadge() {
        // given
        Badge badge = badgeRepository.findByPolicy(Policy.SIGN_UP).getFirst();
        Member member = memberFactory.save(builder -> builder.nickname("우가"));
        String accessToken = authFactory.getAccessTokenByMemberId(member.getId(), Role.USER);
        memberBadgeFactory.save(builder ->
                builder.badge(badge)
                        .member(member)
                        .isAchieved(true)
        );

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .pathParam("badgeId", badge.getId())
                .when().patch("/api/v1/members/me/badges/{badgeId}/representative")
                .then().log().all()
                .statusCode(200);
    }

    @DisplayName("예외: 대표 뱃지가 수정이 될 때 소유하지 않은 뱃지면 예외가 발생한다")
    @Test
    void patchRepresentativeBadge_noOwnBadgeThrowNotFoundException() {
        // given
        Badge badge = badgeRepository.findByPolicy(Policy.SIGN_UP).getFirst();
        Member member = memberFactory.save(builder -> builder.nickname("우가"));
        String accessToken = authFactory.getAccessTokenByMemberId(member.getId(), Role.USER);

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .pathParam("badgeId", badge.getId())
                .when().patch("/api/v1/members/me/badges/{badgeId}/representative")
                .then().log().all()
                .statusCode(404);
    }

    @DisplayName("사용자의 프로필 정보를 조회할 때, 연도와 상관없이 전체 직관 통계를 합산하여 보여준다")
    @Test
    void findProfileInformationWithAllYears() {
        // 1. Given: 기본 환경 설정 (팀, 경기장, 멤버)
        Team ht = teamRepository.findByTeamCode("HT").orElseThrow();
        Team lt = teamRepository.findByTeamCode("LT").orElseThrow();
        Stadium kia = stadiumRepository.findByShortName("챔피언스필드").orElseThrow();
        Badge badge = badgeRepository.findByPolicy(Policy.SIGN_UP).getFirst();

        Member me = memberFactory.save(b -> b.nickname("우가").team(ht));
        Member profileOwner = memberFactory.save(b -> b.nickname("가짜우가")
                .team(ht)
                .representativeBadge(badge));

        String accessToken = authFactory.getAccessTokenByMemberId(me.getId(), Role.USER);

        // 2. Given: 직관 데이터 생성 (과거 연도와 현재 연도 섞기)
        // 2024년 데이터 (1승)
        Game g2024 = gameFactory.save(b -> b.stadium(kia)
                .homeTeam(ht).awayTeam(lt)
                .date(LocalDate.of(2024, 5, 20))
                .homeScore(5).awayScore(2)
                .gameState(GameState.COMPLETED));
        checkInFactory.save(b -> b.game(g2024).member(profileOwner).team(ht));

        // 2026년 데이터 (1승 1패)
        Game g2026_win = gameFactory.save(b -> b.stadium(kia)
                .homeTeam(ht).awayTeam(lt)
                .date(LocalDate.of(2026, 2, 10))
                .homeScore(3).awayScore(1)
                .gameState(GameState.COMPLETED));
        Game g2026_lose = gameFactory.save(b -> b.stadium(kia)
                .homeTeam(ht).awayTeam(lt)
                .date(LocalDate.of(2026, 2, 15)) // 가장 최근 경기
                .homeScore(1).awayScore(4)
                .gameState(GameState.COMPLETED));

        checkInFactory.save(b -> b.game(g2026_win).member(profileOwner).team(ht));
        checkInFactory.save(b -> b.game(g2026_lose).member(profileOwner).team(ht));

        // 3. When: 프로필 조회 API 호출
        var response = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .pathParam("memberId", profileOwner.getId())
                .when().get("/api/v1/members/{memberId}")
                .then().log().all()
                .statusCode(200)
                .extract().as(MemberProfileResponse.class);

        // 4. Then: 데이터 검증 (2024년 + 2026년 통계가 합산되었는지 확인)
        assertSoftly(softly -> {
            softly.assertThat(response.nickname()).isEqualTo("가짜우가");
            // 전체 직관 횟수: 3 (2024년 1건 + 2026년 2건)
            softly.assertThat(response.checkIn().counts()).isEqualTo(3);
            // 전체 승리 횟수: 2 (2024년 1승 + 2026년 1승)
            softly.assertThat(response.checkIn().winCounts()).isEqualTo(2);
            softly.assertThat(response.checkIn().loseCounts()).isEqualTo(1);
            // 최근 직관 날짜: 2026-02-15
            softly.assertThat(response.checkIn().recentCheckInDate()).isEqualTo(LocalDate.of(2026, 2, 15));
            // 승률 계산: (2승 / 3경기) * 100 = 66.7 (소수점 첫째자리 반올림 가정)
            softly.assertThat(response.checkIn().winRate()).isEqualTo(66.7);
        });
    }

    @DisplayName("예외: 프로필 소유자의 회원을 찾을 수 없으면 예외가 발생한다")
    @Test
    void findProfileInformation_notFoundProfileOwnerMember() {
        // given
        Team team = teamRepository.findByTeamCode("HT").orElseThrow();
        Member me = memberFactory.save(builder ->
                builder.nickname("우가")
                        .team(team)
        );
        long invalidProfileOwnerMemberId = 9999999L;
        String accessToken = authFactory.getAccessTokenByMemberId(me.getId(), Role.USER);

        // when
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .pathParam("memberId", invalidProfileOwnerMemberId)
                .when().get("/api/v1/members/{memberId}")
                .then().log().all()
                .statusCode(404);
    }
}
