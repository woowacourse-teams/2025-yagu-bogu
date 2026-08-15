package com.yagubogu.widget;

import com.yagubogu.auth.config.AuthTestConfig;
import com.yagubogu.game.domain.Game;
import com.yagubogu.game.domain.GameState;
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
import com.yagubogu.support.widget.WidgetFactory;
import com.yagubogu.team.domain.Team;
import com.yagubogu.team.repository.TeamRepository;
import com.yagubogu.widget.domain.WidgetDevice;
import com.yagubogu.widget.domain.WidgetPlatform;
import com.yagubogu.widget.repository.WidgetDeviceRepository;
import com.yagubogu.widget.repository.WidgetGamePushRepository;
import com.yagubogu.widget.repository.WidgetLiveActivityRepository;
import com.yagubogu.widget.repository.WidgetSettingsRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.transaction.support.TransactionTemplate;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.hamcrest.Matchers.is;

@Import({AuthTestConfig.class, JpaAuditingConfig.class})
class WidgetE2eTest extends E2eTestBase {

    @LocalServerPort
    private int port;

    @Autowired private AuthFactory authFactory;
    @Autowired private MemberFactory memberFactory;
    @Autowired private GameFactory gameFactory;
    @Autowired private TeamRepository teamRepository;
    @Autowired private StadiumRepository stadiumRepository;
    @Autowired private WidgetDeviceRepository widgetDeviceRepository;
    @Autowired private WidgetLiveActivityRepository widgetLiveActivityRepository;
    @Autowired private WidgetSettingsRepository widgetSettingsRepository;
    @Autowired private WidgetGamePushRepository widgetGamePushRepository;
    @Autowired private EntityManager entityManager;
    @Autowired private TransactionTemplate transactionTemplate;

    private WidgetFactory widgetFactory;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        widgetFactory = new WidgetFactory(
                widgetDeviceRepository, widgetLiveActivityRepository,
                widgetSettingsRepository, widgetGamePushRepository);
    }

    /** 위젯 테이블을 추가로 정리합니다. E2eTestBase.cleanData()보다 먼저 실행됩니다. */
    @AfterEach
    void cleanWidgetData() {
        transactionTemplate.executeWithoutResult(status -> {
            entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate();
            entityManager.createNativeQuery("TRUNCATE TABLE widget_live_activities").executeUpdate();
            entityManager.createNativeQuery("TRUNCATE TABLE widget_devices").executeUpdate();
            entityManager.createNativeQuery("TRUNCATE TABLE widget_settings").executeUpdate();
            entityManager.createNativeQuery("TRUNCATE TABLE widget_game_pushes").executeUpdate();
            entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();
        });
    }

    // ─── 헬퍼 ────────────────────────────────────────────────────────────────

    private String tokenOf(final Member member) {
        return authFactory.getAccessTokenByMemberId(member.getId(), Role.USER);
    }

    private Game savedGame() {
        final Team home = teamRepository.findByTeamCode("OB").orElseThrow();
        final Team away = teamRepository.findByTeamCode("LG").orElseThrow();
        final Stadium stadium = stadiumRepository.findAll().get(0);
        return gameFactory.save(b -> b.stadium(stadium).homeTeam(home).awayTeam(away)
                .date(LocalDate.of(2025, 7, 21)).startAt(LocalTime.of(18, 30))
                .gameState(GameState.SCHEDULED));
    }

    // ─── [API 1] POST /api/v1/widgets/devices ────────────────────────────────

    @DisplayName("[API 1] 디바이스 토큰을 등록하면 204를 반환한다")
    @Test
    void registerDevice_returns204() {
        final Member member = memberFactory.save(MemberBuilder::build);

        given().log().all()
                .header(HttpHeaders.AUTHORIZATION, tokenOf(member))
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "platform", "IOS",
                        "deviceId", "test-device-uuid-1",
                        "pushToken", "apns-push-token-abc",
                        "appVersion", "3.1.0"
                ))
                .when().post("/api/v1/widgets/devices")
                .then().log().all()
                .statusCode(204);

        final WidgetDevice saved = widgetDeviceRepository.findById("test-device-uuid-1").orElseThrow();
        assertSoftly(softly -> {
            softly.assertThat(saved.getMember().getId()).isEqualTo(member.getId());
            softly.assertThat(saved.getPlatform()).isEqualTo(WidgetPlatform.IOS);
            softly.assertThat(saved.getPushToken()).isEqualTo("apns-push-token-abc");
        });
    }

    @DisplayName("[API 1] 같은 deviceId로 재등록하면 토큰이 갱신되고 204를 반환한다 (upsert)")
    @Test
    void registerDevice_updatesToken_returns204() {
        final Member member = memberFactory.save(MemberBuilder::build);
        widgetFactory.saveDevice(member, WidgetPlatform.IOS, "existing-device", "old-token");

        given().log().all()
                .header(HttpHeaders.AUTHORIZATION, tokenOf(member))
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "platform", "IOS",
                        "deviceId", "existing-device",
                        "pushToken", "new-rotated-token"
                ))
                .when().post("/api/v1/widgets/devices")
                .then().log().all()
                .statusCode(204);

        assertThat(widgetDeviceRepository.findAll()).hasSize(1);
        assertThat(widgetDeviceRepository.findById("existing-device").orElseThrow().getPushToken())
                .isEqualTo("new-rotated-token");
    }

    @DisplayName("[API 1] platform 없이 등록하면 400을 반환한다")
    @Test
    void registerDevice_missingPlatform_returns400() {
        final Member member = memberFactory.save(MemberBuilder::build);

        given().log().all()
                .header(HttpHeaders.AUTHORIZATION, tokenOf(member))
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "deviceId", "device-no-platform",
                        "pushToken", "some-token"
                ))
                .when().post("/api/v1/widgets/devices")
                .then().log().all()
                .statusCode(400);
    }

    @DisplayName("[API 1] 인증 토큰 없이 요청하면 401을 반환한다")
    @Test
    void registerDevice_unauthorized_returns401() {
        given().log().all()
                .contentType(ContentType.JSON)
                .body(Map.of("platform", "IOS", "deviceId", "d1", "pushToken", "t1"))
                .when().post("/api/v1/widgets/devices")
                .then().log().all()
                .statusCode(401);
    }

    // ─── [API 3] DELETE /api/v1/widgets/devices/{deviceId} ───────────────────

    @DisplayName("[API 3] 등록된 디바이스를 해제하면 204를 반환한다")
    @Test
    void unregisterDevice_returns204() {
        final Member member = memberFactory.save(MemberBuilder::build);
        widgetFactory.saveDevice(member, WidgetPlatform.ANDROID, "android-device-1", "fcm-token");

        given().log().all()
                .header(HttpHeaders.AUTHORIZATION, tokenOf(member))
                .when().delete("/api/v1/widgets/devices/android-device-1")
                .then().log().all()
                .statusCode(204);

        assertThat(widgetDeviceRepository.findById("android-device-1")).isEmpty();
    }

    @DisplayName("[API 3] 등록되지 않은 deviceId를 해제하면 404를 반환한다")
    @Test
    void unregisterDevice_notFound_returns404() {
        final Member member = memberFactory.save(MemberBuilder::build);

        given().log().all()
                .header(HttpHeaders.AUTHORIZATION, tokenOf(member))
                .when().delete("/api/v1/widgets/devices/no-such-device")
                .then().log().all()
                .statusCode(404);
    }

    // ─── [API 2] POST /api/v1/widgets/live-activities ────────────────────────

    @DisplayName("[API 2] iOS Live Activity 갱신 토큰을 등록하면 204를 반환한다")
    @Test
    void registerLiveActivity_returns204() {
        final Member member = memberFactory.save(MemberBuilder::build);
        widgetFactory.saveDevice(member, WidgetPlatform.IOS, "ios-device-la", "push-token");
        final Game game = savedGame();

        given().log().all()
                .header(HttpHeaders.AUTHORIZATION, tokenOf(member))
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "deviceId", "ios-device-la",
                        "gameId", game.getId(),
                        "activityId", "activity-uuid-123",
                        "updateToken", "update-token-hex-abc"
                ))
                .when().post("/api/v1/widgets/live-activities")
                .then().log().all()
                .statusCode(204);

        assertThat(widgetLiveActivityRepository.findById("activity-uuid-123")).isPresent();
    }

    @DisplayName("[API 2] 등록되지 않은 deviceId로 Live Activity 등록 시 404를 반환한다")
    @Test
    void registerLiveActivity_deviceNotFound_returns404() {
        final Member member = memberFactory.save(MemberBuilder::build);
        final Game game = savedGame();

        given().log().all()
                .header(HttpHeaders.AUTHORIZATION, tokenOf(member))
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "deviceId", "no-such-device",
                        "gameId", game.getId(),
                        "activityId", "activity-xyz",
                        "updateToken", "update-token"
                ))
                .when().post("/api/v1/widgets/live-activities")
                .then().log().all()
                .statusCode(404);
    }

    // ─── [API 2] DELETE /api/v1/widgets/live-activities/{activityId} ─────────

    @DisplayName("[API 2] iOS Live Activity를 해제하면 204를 반환한다")
    @Test
    void unregisterLiveActivity_returns204() {
        final Member member = memberFactory.save(MemberBuilder::build);
        final WidgetDevice device = widgetFactory.saveDevice(member, WidgetPlatform.IOS, "ios-del-device", "pt");
        final Game game = savedGame();
        widgetFactory.saveActivity(device, game, "activity-to-delete", "ut");

        given().log().all()
                .header(HttpHeaders.AUTHORIZATION, tokenOf(member))
                .when().delete("/api/v1/widgets/live-activities/activity-to-delete")
                .then().log().all()
                .statusCode(204);

        assertThat(widgetLiveActivityRepository.findById("activity-to-delete")).isEmpty();
    }

    @DisplayName("[API 2] 존재하지 않는 activityId 해제 시 404를 반환한다")
    @Test
    void unregisterLiveActivity_notFound_returns404() {
        final Member member = memberFactory.save(MemberBuilder::build);

        given().log().all()
                .header(HttpHeaders.AUTHORIZATION, tokenOf(member))
                .when().delete("/api/v1/widgets/live-activities/no-such-activity")
                .then().log().all()
                .statusCode(404);
    }

    // ─── [API 4] GET /api/v1/widgets/settings ────────────────────────────────

    @DisplayName("[API 4] 설정 row가 없을 때 기본값 true를 반환한다")
    @Test
    void getSettings_returnsDefaultTrue() {
        final Member member = memberFactory.save(MemberBuilder::build);

        given().log().all()
                .header(HttpHeaders.AUTHORIZATION, tokenOf(member))
                .when().get("/api/v1/widgets/settings")
                .then().log().all()
                .statusCode(200)
                .body("enabled", is(true));
    }

    @DisplayName("[API 4] enabled=false로 저장된 설정을 반환한다")
    @Test
    void getSettings_returnsFalse_whenDisabled() {
        final Member member = memberFactory.save(MemberBuilder::build);
        widgetFactory.saveSettings(member.getId(), false);

        given().log().all()
                .header(HttpHeaders.AUTHORIZATION, tokenOf(member))
                .when().get("/api/v1/widgets/settings")
                .then().log().all()
                .statusCode(200)
                .body("enabled", is(false));
    }

    // ─── [API 4] PATCH /api/v1/widgets/settings ──────────────────────────────

    @DisplayName("[API 4] 위젯을 끄면 204를 반환하고 DB에 false로 저장된다")
    @Test
    void updateSettings_disablesWidget_returns204() {
        final Member member = memberFactory.save(MemberBuilder::build);

        given().log().all()
                .header(HttpHeaders.AUTHORIZATION, tokenOf(member))
                .contentType(ContentType.JSON)
                .body(Map.of("enabled", false))
                .when().patch("/api/v1/widgets/settings")
                .then().log().all()
                .statusCode(204);

        assertThat(widgetSettingsRepository.findByMemberId(member.getId())
                .orElseThrow().isEnabled()).isFalse();
    }

    @DisplayName("[API 4] false로 꺼진 위젯을 다시 켜면 true로 갱신된다")
    @Test
    void updateSettings_re_enablesWidget() {
        final Member member = memberFactory.save(MemberBuilder::build);
        widgetFactory.saveSettings(member.getId(), false);

        given().log().all()
                .header(HttpHeaders.AUTHORIZATION, tokenOf(member))
                .contentType(ContentType.JSON)
                .body(Map.of("enabled", true))
                .when().patch("/api/v1/widgets/settings")
                .then().log().all()
                .statusCode(204);

        assertThat(widgetSettingsRepository.findByMemberId(member.getId())
                .orElseThrow().isEnabled()).isTrue();
        // row가 추가 생성되지 않아야 함
        assertThat(widgetSettingsRepository.findAll()).hasSize(1);
    }

    @DisplayName("[API 4] enabled 필드 없이 PATCH하면 400을 반환한다")
    @Test
    void updateSettings_missingEnabled_returns400() {
        final Member member = memberFactory.save(MemberBuilder::build);

        given().log().all()
                .header(HttpHeaders.AUTHORIZATION, tokenOf(member))
                .contentType(ContentType.JSON)
                .body(Map.of())
                .when().patch("/api/v1/widgets/settings")
                .then().log().all()
                .statusCode(400);
    }
}
