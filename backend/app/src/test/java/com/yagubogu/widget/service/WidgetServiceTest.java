package com.yagubogu.widget.service;

import com.yagubogu.auth.config.AuthTestConfig;
import com.yagubogu.game.domain.Game;
import com.yagubogu.game.domain.GameState;
import com.yagubogu.game.repository.GameRepository;
import com.yagubogu.global.config.JpaAuditingConfig;
import com.yagubogu.global.exception.NotFoundException;
import com.yagubogu.member.domain.Member;
import com.yagubogu.member.repository.MemberRepository;
import com.yagubogu.stadium.domain.Stadium;
import com.yagubogu.stadium.repository.StadiumRepository;
import com.yagubogu.support.game.GameFactory;
import com.yagubogu.support.member.MemberBuilder;
import com.yagubogu.support.member.MemberFactory;
import com.yagubogu.support.widget.WidgetFactory;
import com.yagubogu.team.domain.Team;
import com.yagubogu.team.repository.TeamRepository;
import com.yagubogu.widget.domain.WidgetDevice;
import com.yagubogu.widget.domain.WidgetGamePush;
import com.yagubogu.widget.domain.WidgetLiveActivity;
import com.yagubogu.widget.domain.WidgetPlatform;
import com.yagubogu.widget.domain.WidgetSettings;
import com.yagubogu.widget.dto.v1.WidgetDeviceRegisterRequest;
import com.yagubogu.widget.dto.v1.WidgetLiveActivityRegisterRequest;
import com.yagubogu.widget.dto.v1.WidgetSettingsPatchRequest;
import com.yagubogu.widget.dto.v1.WidgetSettingsResponse;
import com.yagubogu.widget.repository.WidgetDeviceRepository;
import com.yagubogu.widget.repository.WidgetGamePushRepository;
import com.yagubogu.widget.repository.WidgetLiveActivityRepository;
import com.yagubogu.widget.repository.WidgetSettingsRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@Import({AuthTestConfig.class, JpaAuditingConfig.class})
@DataJpaTest
class WidgetServiceTest {

    private WidgetService widgetService;
    private WidgetFactory widgetFactory;

    @Mock
    private WidgetPushService widgetPushService;

    @Autowired private WidgetDeviceRepository widgetDeviceRepository;
    @Autowired private WidgetLiveActivityRepository widgetLiveActivityRepository;
    @Autowired private WidgetSettingsRepository widgetSettingsRepository;
    @Autowired private WidgetGamePushRepository widgetGamePushRepository;
    @Autowired private MemberRepository memberRepository;
    @Autowired private GameRepository gameRepository;
    @Autowired private TeamRepository teamRepository;
    @Autowired private StadiumRepository stadiumRepository;
    @Autowired private MemberFactory memberFactory;
    @Autowired private GameFactory gameFactory;

    private final Clock fixedClock = Clock.fixed(
            LocalDate.of(2025, 7, 21).atTime(18, 0).atZone(ZoneId.of("Asia/Seoul")).toInstant(),
            ZoneId.of("Asia/Seoul")
    );

    @BeforeEach
    void setUp() {
        widgetFactory = new WidgetFactory(
                widgetDeviceRepository, widgetLiveActivityRepository,
                widgetSettingsRepository, widgetGamePushRepository
        );
        widgetService = new WidgetService(
                widgetDeviceRepository, widgetLiveActivityRepository,
                widgetSettingsRepository, widgetGamePushRepository,
                memberRepository, gameRepository,
                widgetPushService, fixedClock
        );
    }

    // ── 헬퍼 ─────────────────────────────────────────────────────────────────

    private Game savedGame(final Team home, final Team away) {
        Stadium stadium = stadiumRepository.findAll().get(0);
        return gameFactory.save(b -> b.stadium(stadium).homeTeam(home).awayTeam(away)
                .date(LocalDate.of(2025, 7, 21))
                .startAt(LocalTime.of(18, 30))
                .gameState(GameState.SCHEDULED));
    }

    // ── [API 1] registerDevice ────────────────────────────────────────────────

    @DisplayName("디바이스 토큰을 처음 등록하면 새로운 row가 생성된다")
    @Test
    void registerDevice_createsNewDevice() {
        final Member member = memberFactory.save(MemberBuilder::build);
        final WidgetDeviceRegisterRequest request = new WidgetDeviceRegisterRequest(
                WidgetPlatform.IOS, "device-uuid-1", "apns-token-abc", "3.1.0");

        widgetService.registerDevice(member.getId(), request);

        final WidgetDevice saved = widgetDeviceRepository.findById("device-uuid-1").orElseThrow();
        assertSoftly(softly -> {
            softly.assertThat(saved.getMember().getId()).isEqualTo(member.getId());
            softly.assertThat(saved.getPlatform()).isEqualTo(WidgetPlatform.IOS);
            softly.assertThat(saved.getPushToken()).isEqualTo("apns-token-abc");
            softly.assertThat(saved.getAppVersion()).isEqualTo("3.1.0");
        });
    }

    @DisplayName("같은 deviceId로 재등록하면 토큰이 갱신된다 (upsert)")
    @Test
    void registerDevice_updatesTokenOnReregister() {
        final Member member = memberFactory.save(MemberBuilder::build);
        widgetFactory.saveDevice(member, WidgetPlatform.IOS, "device-uuid-1", "old-token");

        final WidgetDeviceRegisterRequest request = new WidgetDeviceRegisterRequest(
                WidgetPlatform.IOS, "device-uuid-1", "new-token", "3.2.0");

        widgetService.registerDevice(member.getId(), request);

        final WidgetDevice updated = widgetDeviceRepository.findById("device-uuid-1").orElseThrow();
        assertSoftly(softly -> {
            softly.assertThat(updated.getPushToken()).isEqualTo("new-token");
            softly.assertThat(updated.getAppVersion()).isEqualTo("3.2.0");
        });
        // DB row는 새로 생성되지 않고 1개여야 함
        assertThat(widgetDeviceRepository.findAll()).hasSize(1);
    }

    @DisplayName("예외: 존재하지 않는 회원 ID로 등록하면 NotFoundException이 발생한다")
    @Test
    void registerDevice_notFoundMember_throwsNotFoundException() {
        final WidgetDeviceRegisterRequest request = new WidgetDeviceRegisterRequest(
                WidgetPlatform.ANDROID, "device-uuid-99", "fcm-token", null);

        assertThatThrownBy(() -> widgetService.registerDevice(9999L, request))
                .isExactlyInstanceOf(NotFoundException.class);
    }

    // ── [API 3] unregisterDevice ──────────────────────────────────────────────

    @DisplayName("디바이스를 해제하면 device row와 연결된 live activity가 함께 삭제된다")
    @Test
    void unregisterDevice_deletesDeviceAndActivities() {
        final Member member = memberFactory.save(MemberBuilder::build);
        final Team home = teamRepository.findByTeamCode("OB").orElseThrow();
        final Team away = teamRepository.findByTeamCode("LG").orElseThrow();
        final Game game = savedGame(home, away);

        final WidgetDevice device = widgetFactory.saveDevice(member, WidgetPlatform.IOS, "device-d1", "token-x");
        widgetFactory.saveActivity(device, game);

        widgetService.unregisterDevice(member.getId(), "device-d1");

        assertSoftly(softly -> {
            softly.assertThat(widgetDeviceRepository.findById("device-d1")).isEmpty();
            softly.assertThat(widgetLiveActivityRepository.findAllByDeviceDeviceId("device-d1")).isEmpty();
        });
    }

    @DisplayName("예외: 등록되지 않은 deviceId를 해제하면 NotFoundException이 발생한다")
    @Test
    void unregisterDevice_notFound_throwsNotFoundException() {
        final Member member = memberFactory.save(MemberBuilder::build);

        assertThatThrownBy(() -> widgetService.unregisterDevice(member.getId(), "no-such-device"))
                .isExactlyInstanceOf(NotFoundException.class);
    }

    // ── [API 2] registerLiveActivity ─────────────────────────────────────────

    @DisplayName("iOS Live Activity 갱신 토큰을 등록한다")
    @Test
    void registerLiveActivity_savesActivity() {
        final Member member = memberFactory.save(MemberBuilder::build);
        final Team home = teamRepository.findByTeamCode("OB").orElseThrow();
        final Team away = teamRepository.findByTeamCode("LG").orElseThrow();
        final Game game = savedGame(home, away);
        final WidgetDevice device = widgetFactory.saveDevice(member, WidgetPlatform.IOS, "device-ios-1", "push-token");

        final WidgetLiveActivityRegisterRequest request = new WidgetLiveActivityRegisterRequest(
                "device-ios-1", game.getId(), "activity-abc", "update-token-xyz");

        widgetService.registerLiveActivity(member.getId(), request);

        final WidgetLiveActivity saved = widgetLiveActivityRepository.findById("activity-abc").orElseThrow();
        assertSoftly(softly -> {
            softly.assertThat(saved.getGame().getId()).isEqualTo(game.getId());
            softly.assertThat(saved.getUpdateToken()).isEqualTo("update-token-xyz");
        });
    }

    @DisplayName("같은 activityId로 재등록하면 updateToken만 갱신된다 (토큰 회전 대응)")
    @Test
    void registerLiveActivity_updatesTokenOnReregister() {
        final Member member = memberFactory.save(MemberBuilder::build);
        final Team home = teamRepository.findByTeamCode("OB").orElseThrow();
        final Team away = teamRepository.findByTeamCode("LG").orElseThrow();
        final Game game = savedGame(home, away);
        final WidgetDevice device = widgetFactory.saveDevice(member, WidgetPlatform.IOS, "device-ios-2", "push-token");
        widgetFactory.saveActivity(device, game, "activity-xyz", "old-update-token");

        final WidgetLiveActivityRegisterRequest request = new WidgetLiveActivityRegisterRequest(
                "device-ios-2", game.getId(), "activity-xyz", "new-update-token");

        widgetService.registerLiveActivity(member.getId(), request);

        final WidgetLiveActivity updated = widgetLiveActivityRepository.findById("activity-xyz").orElseThrow();
        assertThat(updated.getUpdateToken()).isEqualTo("new-update-token");
        assertThat(widgetLiveActivityRepository.findAll()).hasSize(1);
    }

    @DisplayName("예외: 등록되지 않은 deviceId로 Live Activity를 등록하면 NotFoundException이 발생한다")
    @Test
    void registerLiveActivity_deviceNotFound_throwsNotFoundException() {
        final Member member = memberFactory.save(MemberBuilder::build);
        final Team home = teamRepository.findByTeamCode("OB").orElseThrow();
        final Team away = teamRepository.findByTeamCode("LG").orElseThrow();
        final Game game = savedGame(home, away);

        final WidgetLiveActivityRegisterRequest request = new WidgetLiveActivityRegisterRequest(
                "no-such-device", game.getId(), "activity-nope", "token");

        assertThatThrownBy(() -> widgetService.registerLiveActivity(member.getId(), request))
                .isExactlyInstanceOf(NotFoundException.class);
    }

    // ── [API 2] unregisterLiveActivity ───────────────────────────────────────

    @DisplayName("iOS Live Activity를 해제하면 해당 row가 삭제된다")
    @Test
    void unregisterLiveActivity_deletesActivity() {
        final Member member = memberFactory.save(MemberBuilder::build);
        final Team home = teamRepository.findByTeamCode("OB").orElseThrow();
        final Team away = teamRepository.findByTeamCode("LG").orElseThrow();
        final Game game = savedGame(home, away);
        final WidgetDevice device = widgetFactory.saveDevice(member, WidgetPlatform.IOS, "device-ios-3", "pt");
        widgetFactory.saveActivity(device, game, "activity-del", "ut");

        widgetService.unregisterLiveActivity(member.getId(), "activity-del");

        assertThat(widgetLiveActivityRepository.findById("activity-del")).isEmpty();
    }

    @DisplayName("예외: 다른 회원의 Live Activity를 해제하려 하면 NotFoundException이 발생한다")
    @Test
    void unregisterLiveActivity_otherMembersActivity_throwsNotFoundException() {
        final Member owner = memberFactory.save(MemberBuilder::build);
        final Member other = memberFactory.save(MemberBuilder::build);
        final Team home = teamRepository.findByTeamCode("OB").orElseThrow();
        final Team away = teamRepository.findByTeamCode("LG").orElseThrow();
        final Game game = savedGame(home, away);
        final WidgetDevice device = widgetFactory.saveDevice(owner, WidgetPlatform.IOS, "device-owner", "pt");
        widgetFactory.saveActivity(device, game, "activity-owned", "ut");

        assertThatThrownBy(() -> widgetService.unregisterLiveActivity(other.getId(), "activity-owned"))
                .isExactlyInstanceOf(NotFoundException.class);
    }

    // ── [API 4] 설정 조회/변경 ────────────────────────────────────────────────

    @DisplayName("설정 row가 없을 때 getSettings는 기본값 true를 반환한다")
    @Test
    void getSettings_returnsDefaultTrue_whenNoRow() {
        final Member member = memberFactory.save(MemberBuilder::build);

        final WidgetSettingsResponse response = widgetService.getSettings(member.getId());

        assertThat(response.enabled()).isTrue();
    }

    @DisplayName("설정 row가 있을 때 저장된 값을 반환한다")
    @Test
    void getSettings_returnsStoredValue() {
        final Member member = memberFactory.save(MemberBuilder::build);
        widgetFactory.saveSettings(member.getId(), false);

        final WidgetSettingsResponse response = widgetService.getSettings(member.getId());

        assertThat(response.enabled()).isFalse();
    }

    @DisplayName("설정 row가 없을 때 updateSettings를 호출하면 새 row를 생성한다")
    @Test
    void updateSettings_createsRowWhenNotExists() {
        final Member member = memberFactory.save(MemberBuilder::build);

        widgetService.updateSettings(member.getId(), new WidgetSettingsPatchRequest(false));

        final WidgetSettings saved = widgetSettingsRepository.findByMemberId(member.getId()).orElseThrow();
        assertThat(saved.isEnabled()).isFalse();
    }

    @DisplayName("설정 row가 이미 있을 때 updateSettings를 호출하면 값이 갱신된다")
    @Test
    void updateSettings_updatesExistingRow() {
        final Member member = memberFactory.save(MemberBuilder::build);
        widgetFactory.saveSettings(member.getId(), false);

        widgetService.updateSettings(member.getId(), new WidgetSettingsPatchRequest(true));

        final WidgetSettings updated = widgetSettingsRepository.findByMemberId(member.getId()).orElseThrow();
        assertThat(updated.isEnabled()).isTrue();
        assertThat(widgetSettingsRepository.findAll()).hasSize(1);
    }

    // ── sendStartPush ─────────────────────────────────────────────────────────

    @DisplayName("START 푸시 발송: 홈팀 팬 iOS 기기에 푸시를 발송한다")
    @Test
    void sendStartPush_sendsToEligibleDevices() {
        final Team home = teamRepository.findByTeamCode("OB").orElseThrow();
        final Team away = teamRepository.findByTeamCode("LG").orElseThrow();
        final Member homeFan = memberFactory.save(b -> b.team(home));
        widgetFactory.saveDevice(homeFan, WidgetPlatform.IOS);
        final Game game = savedGame(home, away);

        widgetService.sendStartPush(game);

        verify(widgetPushService).sendStart(any(), any());
    }

    @DisplayName("START 푸시 발송: 이미 활성화된 Live Activity가 있는 iOS 기기는 제외된다 (더블헤더 안전장치)")
    @Test
    void sendStartPush_skipsIosDeviceWithActiveLiveActivity() {
        final Team home = teamRepository.findByTeamCode("OB").orElseThrow();
        final Team away = teamRepository.findByTeamCode("LG").orElseThrow();
        final Member homeFan = memberFactory.save(b -> b.team(home));
        final WidgetDevice iosDevice = widgetFactory.saveDevice(homeFan, WidgetPlatform.IOS);

        // 이미 앞 경기의 Live Activity가 활성화되어 있음
        final Game previousGame = savedGame(home, away);
        widgetFactory.saveActivity(iosDevice, previousGame);

        final Game newGame = savedGame(home, away);

        widgetService.sendStartPush(newGame);

        // iOS 기기는 제외되므로 대상이 없어 sendStart가 빈 리스트로 호출되거나 호출되지 않아야 함
        // 실제로는 대상 0개이면 early-return 후 sendStart 호출됨 (count 로깅 후 return)
        // WidgetService 구현상 targets.isEmpty()이면 바로 return → verify never
        verify(widgetPushService, never()).sendStart(anyList(), any());
    }

    @DisplayName("START 푸시 발송: 위젯을 끈 회원(enabled=false)의 기기는 제외된다")
    @Test
    void sendStartPush_excludesDevicesOfDisabledMembers() {
        final Team home = teamRepository.findByTeamCode("OB").orElseThrow();
        final Team away = teamRepository.findByTeamCode("LG").orElseThrow();
        final Member disabledFan = memberFactory.save(b -> b.team(home));
        widgetFactory.saveDevice(disabledFan, WidgetPlatform.ANDROID);
        widgetFactory.saveSettings(disabledFan.getId(), false);
        final Game game = savedGame(home, away);

        widgetService.sendStartPush(game);

        verify(widgetPushService, never()).sendStart(anyList(), any());
    }

    @DisplayName("START 푸시 발송: 이미 발송된 경기에 재시도하면 중복 발송되지 않는다")
    @Test
    void sendStartPush_idempotent_doesNotSendTwice() {
        final Team home = teamRepository.findByTeamCode("OB").orElseThrow();
        final Team away = teamRepository.findByTeamCode("LG").orElseThrow();
        final Member homeFan = memberFactory.save(b -> b.team(home));
        widgetFactory.saveDevice(homeFan, WidgetPlatform.ANDROID);
        final Game game = savedGame(home, away);
        widgetFactory.saveStartSentPush(game.getId());

        widgetService.sendStartPush(game);

        verify(widgetPushService, never()).sendStart(anyList(), any());
    }

    // ── sendEndPush ───────────────────────────────────────────────────────────

    @DisplayName("END 푸시 발송: iOS Live Activity를 종료하고 DB 행을 정리한다")
    @Test
    void sendEndPush_sendsEndAndCleansActivities() {
        final Team home = teamRepository.findByTeamCode("OB").orElseThrow();
        final Team away = teamRepository.findByTeamCode("LG").orElseThrow();
        final Member homeFan = memberFactory.save(b -> b.team(home));
        final Stadium stadium = stadiumRepository.findAll().get(0);
        final Game game = gameFactory.save(b -> b.stadium(stadium).homeTeam(home).awayTeam(away)
                .date(LocalDate.of(2025, 7, 21)).startAt(LocalTime.of(18, 30))
                .homeScore(5).awayScore(3).gameState(GameState.COMPLETED));

        final WidgetDevice device = widgetFactory.saveDevice(homeFan, WidgetPlatform.IOS);
        widgetFactory.saveActivity(device, game);
        widgetFactory.saveStartSentPush(game.getId());

        widgetService.sendEndPush(game);

        assertSoftly(softly -> {
            softly.assertThat(widgetLiveActivityRepository.findAllByGameId(game.getId())).isEmpty();
            final WidgetGamePush push = widgetGamePushRepository.findById(game.getId()).orElseThrow();
            softly.assertThat(push.isEndSent()).isTrue();
        });
        verify(widgetPushService).sendEnd(any(), any(), any());
    }

    @DisplayName("END 푸시 발송: START가 발송된 적 없는 경기에는 END를 보내지 않는다")
    @Test
    void sendEndPush_skipsIfStartNotSent() {
        final Team home = teamRepository.findByTeamCode("OB").orElseThrow();
        final Team away = teamRepository.findByTeamCode("LG").orElseThrow();
        final Game game = savedGame(home, away);

        widgetService.sendEndPush(game);

        verify(widgetPushService, never()).sendEnd(anyList(), anyList(), any());
    }

    @DisplayName("END 푸시 발송: 이미 END를 발송한 경기에 재시도하면 중복 발송되지 않는다")
    @Test
    void sendEndPush_idempotent_doesNotSendTwice() {
        final Team home = teamRepository.findByTeamCode("OB").orElseThrow();
        final Team away = teamRepository.findByTeamCode("LG").orElseThrow();
        final Game game = savedGame(home, away);
        final WidgetGamePush push = widgetFactory.saveStartSentPush(game.getId());
        push.markEndSent(java.time.LocalDateTime.now());
        widgetGamePushRepository.save(push);

        widgetService.sendEndPush(game);

        verify(widgetPushService, never()).sendEnd(anyList(), anyList(), any());
    }

    // ── 매핑 로직: findDevicesForGame ────────────────────────────────────────

    @DisplayName("매핑: 탈퇴한 회원의 디바이스는 START 대상에서 제외된다")
    @Test
    void findDevicesForGame_excludesDeletedMembers() {
        final Team home = teamRepository.findByTeamCode("OB").orElseThrow();
        final Team away = teamRepository.findByTeamCode("LG").orElseThrow();

        // 활성 팬
        final Member activeFan = memberFactory.save(b -> b.team(home));
        widgetFactory.saveDevice(activeFan, WidgetPlatform.ANDROID);

        // 탈퇴한 팬
        final Member deletedFan = memberFactory.save(b -> b.team(home));
        widgetFactory.saveDevice(deletedFan, WidgetPlatform.ANDROID);
        deletedFan.delete();
        memberRepository.save(deletedFan);

        final Game game = savedGame(home, away);
        widgetService.sendStartPush(game);

        // activeFan 1명 기기만 포함 → sendStart 호출됨
        verify(widgetPushService).sendStart(
                argThat(devices -> devices.size() == 1),
                any()
        );
    }

    @DisplayName("매핑: 원정팀 팬의 디바이스도 START 대상에 포함된다")
    @Test
    void findDevicesForGame_includesAwayTeamFans() {
        final Team home = teamRepository.findByTeamCode("OB").orElseThrow();
        final Team away = teamRepository.findByTeamCode("LG").orElseThrow();

        final Member homeFan = memberFactory.save(b -> b.team(home));
        final Member awayFan = memberFactory.save(b -> b.team(away));
        widgetFactory.saveDevice(homeFan, WidgetPlatform.ANDROID);
        widgetFactory.saveDevice(awayFan, WidgetPlatform.ANDROID);

        final Game game = savedGame(home, away);
        widgetService.sendStartPush(game);

        verify(widgetPushService).sendStart(
                argThat(devices -> devices.size() == 2),
                any()
        );
    }

    // Mockito argThat 헬퍼
    private static <T> T argThat(final org.mockito.ArgumentMatcher<T> matcher) {
        return org.mockito.ArgumentMatchers.argThat(matcher);
    }
}
