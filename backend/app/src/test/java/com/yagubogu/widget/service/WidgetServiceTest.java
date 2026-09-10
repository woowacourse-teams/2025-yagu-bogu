package com.yagubogu.widget.service;

import com.yagubogu.auth.config.AuthTestConfig;
import com.yagubogu.game.domain.Game;
import com.yagubogu.game.repository.GameRepository;
import com.yagubogu.global.config.JpaAuditingConfig;
import com.yagubogu.global.exception.BadRequestException;
import com.yagubogu.global.exception.NotFoundException;
import com.yagubogu.member.domain.Member;
import com.yagubogu.member.repository.MemberRepository;
import com.yagubogu.stadium.domain.Stadium;
import com.yagubogu.stadium.repository.StadiumRepository;
import com.yagubogu.support.game.GameFactory;
import com.yagubogu.support.member.MemberBuilder;
import com.yagubogu.support.member.MemberFactory;
import com.yagubogu.team.domain.Team;
import com.yagubogu.team.repository.TeamRepository;
import com.yagubogu.widget.domain.WidgetDevice;
import com.yagubogu.widget.domain.WidgetLiveActivity;
import com.yagubogu.widget.domain.WidgetPlatform;
import com.yagubogu.widget.dto.v1.WidgetDeviceRegisterRequest;
import com.yagubogu.widget.dto.v1.WidgetLiveActivityRegisterRequest;
import com.yagubogu.widget.dto.v1.WidgetSettingsRequest;
import com.yagubogu.widget.repository.WidgetDeviceRepository;
import com.yagubogu.widget.repository.WidgetLiveActivityRepository;
import jakarta.persistence.EntityManager;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Import({AuthTestConfig.class, JpaAuditingConfig.class})
@DataJpaTest
class WidgetServiceTest {

    private WidgetService widgetService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private WidgetDeviceRepository widgetDeviceRepository;

    @Autowired
    private WidgetLiveActivityRepository widgetLiveActivityRepository;

    @Autowired
    private MemberFactory memberFactory;

    @Autowired
    private GameFactory gameFactory;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private StadiumRepository stadiumRepository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        widgetService = new WidgetService(
                memberRepository,
                gameRepository,
                widgetDeviceRepository,
                widgetLiveActivityRepository
        );
    }

    @DisplayName("디바이스를 등록하고 같은 deviceId의 토큰을 갱신한다")
    @Test
    void registerDevice_upsert() {
        Member member = memberFactory.save(MemberBuilder::build);
        String deviceId = UUID.randomUUID().toString();

        widgetService.registerDevice(member.getId(), deviceRequest(deviceId, "old-token", WidgetPlatform.IOS));
        widgetService.registerDevice(member.getId(), deviceRequest(deviceId, "new-token", WidgetPlatform.IOS));

        assertThat(widgetDeviceRepository.count()).isEqualTo(1);
        WidgetDevice device = widgetDeviceRepository.findByDeviceId(deviceId).orElseThrow();
        assertThat(device.getPushToken()).isEqualTo("new-token");
    }

    @DisplayName("같은 앱 설치에서 로그인 회원이 바뀌면 소유권을 이전하고 기존 활동을 제거한다")
    @Test
    void registerDevice_changedOwner() {
        Member oldMember = memberFactory.save(MemberBuilder::build);
        Member newMember = memberFactory.save(MemberBuilder::build);
        Game game = saveGame();
        String deviceId = UUID.randomUUID().toString();

        widgetService.registerDevice(oldMember.getId(), deviceRequest(deviceId, "old-token", WidgetPlatform.IOS));
        widgetService.registerLiveActivity(
                oldMember.getId(),
                activityRequest(deviceId, game.getId(), "activity", "update-token")
        );

        widgetService.registerDevice(newMember.getId(), deviceRequest(deviceId, "new-token", WidgetPlatform.IOS));

        WidgetDevice device = widgetDeviceRepository.findByDeviceId(deviceId).orElseThrow();
        assertThat(device.getMember().getId()).isEqualTo(newMember.getId());
        assertThat(widgetLiveActivityRepository.count()).isZero();
    }

    @DisplayName("Android 디바이스에는 Live Activity를 등록할 수 없다")
    @Test
    void registerLiveActivity_androidDevice() {
        Member member = memberFactory.save(MemberBuilder::build);
        Game game = saveGame();
        String deviceId = UUID.randomUUID().toString();
        widgetService.registerDevice(member.getId(), deviceRequest(deviceId, "fcm-token", WidgetPlatform.ANDROID));

        assertThatThrownBy(() -> widgetService.registerLiveActivity(
                member.getId(),
                activityRequest(deviceId, game.getId(), "activity", "update-token")
        )).isExactlyInstanceOf(BadRequestException.class)
                .hasMessage("Live Activity is available only on IOS devices");
    }

    @DisplayName("같은 디바이스와 경기의 Live Activity 토큰을 갱신한다")
    @Test
    void registerLiveActivity_upsert() {
        Member member = memberFactory.save(MemberBuilder::build);
        Game game = saveGame();
        String deviceId = UUID.randomUUID().toString();
        widgetService.registerDevice(member.getId(), deviceRequest(deviceId, "start-token", WidgetPlatform.IOS));

        widgetService.registerLiveActivity(
                member.getId(),
                activityRequest(deviceId, game.getId(), "activity-1", "old-update-token")
        );
        widgetService.registerLiveActivity(
                member.getId(),
                activityRequest(deviceId, game.getId(), "activity-1", "new-update-token")
        );

        assertThat(widgetLiveActivityRepository.count()).isEqualTo(1);
        WidgetLiveActivity activity = widgetLiveActivityRepository.findByActivityId("activity-1").orElseThrow();
        assertThat(activity.getUpdateToken()).isEqualTo("new-update-token");
    }

    @DisplayName("다른 회원의 Live Activity를 삭제할 수 없다")
    @Test
    void removeLiveActivity_otherMember() {
        Member owner = memberFactory.save(MemberBuilder::build);
        Member other = memberFactory.save(MemberBuilder::build);
        Game game = saveGame();
        String deviceId = UUID.randomUUID().toString();
        widgetService.registerDevice(owner.getId(), deviceRequest(deviceId, "start-token", WidgetPlatform.IOS));
        widgetService.registerLiveActivity(
                owner.getId(),
                activityRequest(deviceId, game.getId(), "activity", "update-token")
        );

        assertThatThrownBy(() -> widgetService.removeLiveActivity(other.getId(), "activity"))
                .isExactlyInstanceOf(NotFoundException.class)
                .hasMessage("Live Activity is not found");
        assertThat(widgetLiveActivityRepository.count()).isEqualTo(1);
    }

    @DisplayName("디바이스를 삭제하면 연결된 Live Activity도 삭제한다")
    @Test
    void removeDevice() {
        Member member = memberFactory.save(MemberBuilder::build);
        Game game = saveGame();
        String deviceId = UUID.randomUUID().toString();
        widgetService.registerDevice(member.getId(), deviceRequest(deviceId, "start-token", WidgetPlatform.IOS));
        widgetService.registerLiveActivity(
                member.getId(),
                activityRequest(deviceId, game.getId(), "activity", "update-token")
        );

        widgetService.removeDevice(member.getId(), deviceId);
        entityManager.flush();

        assertThat(widgetDeviceRepository.count()).isZero();
        assertThat(widgetLiveActivityRepository.count()).isZero();
    }

    @DisplayName("위젯 사용 설정은 디바이스별로 기본 true이고 독립적으로 변경할 수 있다")
    @Test
    void settings() {
        Member member = memberFactory.save(MemberBuilder::build);
        String disabledDeviceId = UUID.randomUUID().toString();
        String enabledDeviceId = UUID.randomUUID().toString();
        widgetService.registerDevice(
                member.getId(),
                deviceRequest(disabledDeviceId, "disabled-device-token", WidgetPlatform.IOS)
        );
        widgetService.registerDevice(
                member.getId(),
                deviceRequest(enabledDeviceId, "enabled-device-token", WidgetPlatform.ANDROID)
        );

        assertThat(widgetService.findSettings(member.getId(), disabledDeviceId).enabled()).isTrue();
        assertThat(widgetService.findSettings(member.getId(), enabledDeviceId).enabled()).isTrue();

        assertThat(widgetService.updateSettings(
                member.getId(),
                disabledDeviceId,
                new WidgetSettingsRequest(false)
        ).enabled()).isFalse();
        assertThat(widgetService.findSettings(member.getId(), disabledDeviceId).enabled()).isFalse();
        assertThat(widgetService.findSettings(member.getId(), enabledDeviceId).enabled()).isTrue();

        widgetService.registerDevice(
                member.getId(),
                deviceRequest(disabledDeviceId, "rotated-token", WidgetPlatform.IOS)
        );
        assertThat(widgetService.findSettings(member.getId(), disabledDeviceId).enabled()).isFalse();
    }

    @DisplayName("다른 회원의 디바이스 위젯 설정을 조회할 수 없다")
    @Test
    void findSettings_otherMember() {
        Member owner = memberFactory.save(MemberBuilder::build);
        Member other = memberFactory.save(MemberBuilder::build);
        String deviceId = UUID.randomUUID().toString();
        widgetService.registerDevice(owner.getId(), deviceRequest(deviceId, "push-token", WidgetPlatform.IOS));

        assertThatThrownBy(() -> widgetService.findSettings(other.getId(), deviceId))
                .isExactlyInstanceOf(NotFoundException.class)
                .hasMessage("Widget device is not found");
    }

    private WidgetDeviceRegisterRequest deviceRequest(
            final String deviceId,
            final String pushToken,
            final WidgetPlatform platform
    ) {
        return new WidgetDeviceRegisterRequest(platform, deviceId, pushToken, "3.1.0");
    }

    private WidgetLiveActivityRegisterRequest activityRequest(
            final String deviceId,
            final long gameId,
            final String activityId,
            final String updateToken
    ) {
        return new WidgetLiveActivityRegisterRequest(deviceId, gameId, activityId, updateToken);
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
