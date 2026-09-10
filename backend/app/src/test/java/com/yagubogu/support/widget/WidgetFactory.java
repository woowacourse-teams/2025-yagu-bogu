package com.yagubogu.support.widget;

import com.yagubogu.game.domain.Game;
import com.yagubogu.member.domain.Member;
import com.yagubogu.widget.domain.WidgetDevice;
import com.yagubogu.widget.domain.WidgetGamePush;
import com.yagubogu.widget.domain.WidgetLiveActivity;
import com.yagubogu.widget.domain.WidgetPlatform;
import com.yagubogu.widget.domain.WidgetSettings;
import com.yagubogu.widget.repository.WidgetDeviceRepository;
import com.yagubogu.widget.repository.WidgetGamePushRepository;
import com.yagubogu.widget.repository.WidgetLiveActivityRepository;
import com.yagubogu.widget.repository.WidgetSettingsRepository;
import java.time.LocalDateTime;
import java.util.UUID;

public class WidgetFactory {

    private final WidgetDeviceRepository deviceRepository;
    private final WidgetLiveActivityRepository activityRepository;
    private final WidgetSettingsRepository settingsRepository;
    private final WidgetGamePushRepository gamePushRepository;

    public WidgetFactory(final WidgetDeviceRepository deviceRepository,
                         final WidgetLiveActivityRepository activityRepository,
                         final WidgetSettingsRepository settingsRepository,
                         final WidgetGamePushRepository gamePushRepository) {
        this.deviceRepository = deviceRepository;
        this.activityRepository = activityRepository;
        this.settingsRepository = settingsRepository;
        this.gamePushRepository = gamePushRepository;
    }

    public WidgetDevice saveDevice(final Member member, final WidgetPlatform platform) {
        return deviceRepository.save(
                new WidgetDevice(UUID.randomUUID().toString(), member, platform,
                        "push-token-" + UUID.randomUUID(), "3.0.0"));
    }

    public WidgetDevice saveDevice(final Member member, final WidgetPlatform platform,
                                   final String deviceId, final String pushToken) {
        return deviceRepository.save(
                new WidgetDevice(deviceId, member, platform, pushToken, "3.0.0"));
    }

    public WidgetLiveActivity saveActivity(final WidgetDevice device, final Game game) {
        return activityRepository.save(
                new WidgetLiveActivity(UUID.randomUUID().toString(), device, game,
                        "update-token-" + UUID.randomUUID()));
    }

    public WidgetLiveActivity saveActivity(final WidgetDevice device, final Game game,
                                           final String activityId, final String updateToken) {
        return activityRepository.save(
                new WidgetLiveActivity(activityId, device, game, updateToken));
    }

    public WidgetSettings saveSettings(final Long memberId, final boolean enabled) {
        return settingsRepository.save(new WidgetSettings(memberId, enabled));
    }

    /** START 발송 완료 상태의 WidgetGamePush를 저장합니다. */
    public WidgetGamePush saveStartSentPush(final Long gameId) {
        final WidgetGamePush push = new WidgetGamePush(gameId);
        push.markStartSent(LocalDateTime.now());
        return gamePushRepository.save(push);
    }
}
