package com.yagubogu.widget;

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
import com.yagubogu.team.domain.Team;
import com.yagubogu.team.repository.TeamRepository;
import com.yagubogu.widget.domain.WidgetDevice;
import com.yagubogu.widget.domain.WidgetPlatform;
import com.yagubogu.widget.dto.v1.WidgetDeviceRegisterRequest;
import com.yagubogu.widget.dto.v1.WidgetLiveActivityRegisterRequest;
import com.yagubogu.widget.dto.v1.WidgetSettingsRequest;
import com.yagubogu.widget.dto.v1.WidgetSettingsResponse;
import com.yagubogu.widget.repository.WidgetDeviceRepository;
import com.yagubogu.widget.repository.WidgetLiveActivityRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;

import static org.assertj.core.api.Assertions.assertThat;

@Import({AuthTestConfig.class, JpaAuditingConfig.class})
class WidgetE2eTest extends E2eTestBase {

    @LocalServerPort
    private int port;

    @Autowired
    private MemberFactory memberFactory;

    @Autowired
    private AuthFactory authFactory;

    @Autowired
    private GameFactory gameFactory;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private StadiumRepository stadiumRepository;

    @Autowired
    private WidgetDeviceRepository widgetDeviceRepository;

    @Autowired
    private WidgetLiveActivityRepository widgetLiveActivityRepository;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @DisplayName("디바이스 푸시 토큰을 등록하고 갱신한다")
    @Test
    void registerDevice() {
        Member member = memberFactory.save(MemberBuilder::build);
        String accessToken = accessToken(member);
        String deviceId = UUID.randomUUID().toString();

        registerDevice(accessToken, deviceId, "old-token", WidgetPlatform.IOS);
        registerDevice(accessToken, deviceId, "new-token", WidgetPlatform.IOS);

        assertThat(widgetDeviceRepository.count()).isEqualTo(1);
        assertThat(widgetDeviceRepository.findByDeviceId(deviceId).orElseThrow().getPushToken())
                .isEqualTo("new-token");
    }

    @DisplayName("iOS Live Activity를 등록하고 해제한다")
    @Test
    void registerAndRemoveLiveActivity() {
        Member member = memberFactory.save(MemberBuilder::build);
        String accessToken = accessToken(member);
        String deviceId = UUID.randomUUID().toString();
        String activityId = UUID.randomUUID().toString();
        Game game = saveGame();
        registerDevice(accessToken, deviceId, "start-token", WidgetPlatform.IOS);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .body(new WidgetLiveActivityRegisterRequest(
                        deviceId,
                        game.getId(),
                        activityId,
                        "update-token"
                ))
                .when().post("/api/v1/widgets/live-activities")
                .then().log().all()
                .statusCode(204);

        assertThat(widgetLiveActivityRepository.findByActivityId(activityId)).isPresent();

        RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .pathParam("activityId", activityId)
                .when().delete("/api/v1/widgets/live-activities/{activityId}")
                .then().log().all()
                .statusCode(204);

        assertThat(widgetLiveActivityRepository.findByActivityId(activityId)).isEmpty();
    }

    @DisplayName("디바이스를 해제하면 연결된 Live Activity도 제거한다")
    @Test
    void removeDevice() {
        Member member = memberFactory.save(MemberBuilder::build);
        String accessToken = accessToken(member);
        String deviceId = UUID.randomUUID().toString();
        String activityId = UUID.randomUUID().toString();
        Game game = saveGame();
        registerDevice(accessToken, deviceId, "start-token", WidgetPlatform.IOS);
        registerActivity(accessToken, deviceId, game.getId(), activityId);

        RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .pathParam("deviceId", deviceId)
                .when().delete("/api/v1/widgets/devices/{deviceId}")
                .then().log().all()
                .statusCode(204);

        assertThat(widgetDeviceRepository.findByDeviceId(deviceId)).isEmpty();
        assertThat(widgetLiveActivityRepository.findByActivityId(activityId)).isEmpty();
    }

    @DisplayName("위젯 사용 설정을 디바이스별로 조회하고 변경한다")
    @Test
    void settings() {
        Member member = memberFactory.save(MemberBuilder::build);
        String accessToken = accessToken(member);
        String disabledDeviceId = UUID.randomUUID().toString();
        String enabledDeviceId = UUID.randomUUID().toString();
        registerDevice(accessToken, disabledDeviceId, "disabled-device-token", WidgetPlatform.IOS);
        registerDevice(accessToken, enabledDeviceId, "enabled-device-token", WidgetPlatform.ANDROID);

        WidgetSettingsResponse initial = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .pathParam("deviceId", disabledDeviceId)
                .when().get("/api/v1/widgets/devices/{deviceId}/settings")
                .then().log().all()
                .statusCode(200)
                .extract().as(WidgetSettingsResponse.class);

        WidgetSettingsResponse updated = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .pathParam("deviceId", disabledDeviceId)
                .body(new WidgetSettingsRequest(false))
                .when().patch("/api/v1/widgets/devices/{deviceId}/settings")
                .then().log().all()
                .statusCode(200)
                .extract().as(WidgetSettingsResponse.class);

        WidgetSettingsResponse otherDevice = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .pathParam("deviceId", enabledDeviceId)
                .when().get("/api/v1/widgets/devices/{deviceId}/settings")
                .then().log().all()
                .statusCode(200)
                .extract().as(WidgetSettingsResponse.class);

        assertThat(initial.enabled()).isTrue();
        assertThat(updated.enabled()).isFalse();
        assertThat(otherDevice.enabled()).isTrue();
    }

    @DisplayName("잘못된 deviceId 형식은 400을 반환한다")
    @Test
    void registerDevice_invalidDeviceId() {
        Member member = memberFactory.save(MemberBuilder::build);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken(member))
                .body(new WidgetDeviceRegisterRequest(WidgetPlatform.IOS, "invalid", "token", null))
                .when().post("/api/v1/widgets/devices")
                .then().log().all()
                .statusCode(400);
    }

    @DisplayName("인증 없이 위젯 API를 호출하면 401을 반환한다")
    @Test
    void findSettings_unauthorized() {
        String deviceId = UUID.randomUUID().toString();

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .pathParam("deviceId", deviceId)
                .when().get("/api/v1/widgets/devices/{deviceId}/settings")
                .then().log().all()
                .statusCode(401);
    }

    @DisplayName("다른 회원의 Live Activity를 해제하면 404를 반환한다")
    @Test
    void removeLiveActivity_otherMember() {
        Member owner = memberFactory.save(MemberBuilder::build);
        Member other = memberFactory.save(MemberBuilder::build);
        String ownerToken = accessToken(owner);
        String deviceId = UUID.randomUUID().toString();
        String activityId = UUID.randomUUID().toString();
        Game game = saveGame();
        registerDevice(ownerToken, deviceId, "start-token", WidgetPlatform.IOS);
        registerActivity(ownerToken, deviceId, game.getId(), activityId);

        RestAssured.given().log().all()
                .header(HttpHeaders.AUTHORIZATION, accessToken(other))
                .pathParam("activityId", activityId)
                .when().delete("/api/v1/widgets/live-activities/{activityId}")
                .then().log().all()
                .statusCode(404);

        assertThat(widgetLiveActivityRepository.findByActivityId(activityId)).isPresent();
    }

    private void registerDevice(
            final String accessToken,
            final String deviceId,
            final String pushToken,
            final WidgetPlatform platform
    ) {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .body(new WidgetDeviceRegisterRequest(platform, deviceId, pushToken, "3.1.0"))
                .when().post("/api/v1/widgets/devices")
                .then().log().all()
                .statusCode(204);
    }

    private void registerActivity(
            final String accessToken,
            final String deviceId,
            final long gameId,
            final String activityId
    ) {
        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .body(new WidgetLiveActivityRegisterRequest(deviceId, gameId, activityId, "update-token"))
                .when().post("/api/v1/widgets/live-activities")
                .then().log().all()
                .statusCode(204);
    }

    private String accessToken(final Member member) {
        return authFactory.getAccessTokenByMemberId(member.getId(), Role.USER);
    }

    private Game saveGame() {
        Team homeTeam = teamRepository.findByTeamCode("HT").orElseThrow();
        Team awayTeam = teamRepository.findByTeamCode("LG").orElseThrow();
        Stadium stadium = stadiumRepository.findAll().getFirst();

        return gameFactory.save(builder -> builder
                .homeTeam(homeTeam)
                .awayTeam(awayTeam)
                .stadium(stadium));
    }
}
