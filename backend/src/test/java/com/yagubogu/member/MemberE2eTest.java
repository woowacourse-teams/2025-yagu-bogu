package com.yagubogu.member;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.yagubogu.auth.config.AuthTestConfig;
import com.yagubogu.global.config.JpaAuditingConfig;
import com.yagubogu.member.domain.Member;
import com.yagubogu.member.domain.Role;
import com.yagubogu.member.dto.MemberFavoriteRequest;
import com.yagubogu.member.dto.MemberFavoriteResponse;
import com.yagubogu.member.dto.MemberInfoResponse;
import com.yagubogu.member.dto.MemberNicknameRequest;
import com.yagubogu.member.dto.MemberNicknameResponse;
import com.yagubogu.support.E2eTestBase;
import com.yagubogu.support.auth.AuthFactory;
import com.yagubogu.support.member.MemberBuilder;
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
public class MemberE2eTest extends E2eTestBase {

    @LocalServerPort
    private int port;

    @Autowired
    private MemberFactory memberFactory;

    @Autowired
    private AuthFactory authFactory;

    @Autowired
    private TeamRepository teamRepository;

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
                .when().get("/api/members/favorites")
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
                .when().get("/api/members/me/nickname")
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
                .when().patch("/api/members/me/nickname")
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
                .when().patch("/api/members/me/nickname")
                .then().log().all()
                .statusCode(409);
    }

    @DisplayName("예외: 닉네임 수정 시 길이 제한을 초과하면 422 상태를 반환한다")
    @Test
    void patchNickname_nickNameTooLongReturn422Status() {
        // given
        Member member2 = memberFactory.save(builder -> builder.nickname("우가"));
        String longNickName = "1234567890123";

        String accessToken = authFactory.getAccessTokenByMemberId(member2.getId(), Role.USER);

        // when & then
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .body(new MemberNicknameRequest(longNickName))
                .when().patch("/api/members/me/nickname")
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
                .when().delete("/api/members/me")
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
                .when().patch("/api/members/favorites")
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
                .when().get("/api/members/me")
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
}
