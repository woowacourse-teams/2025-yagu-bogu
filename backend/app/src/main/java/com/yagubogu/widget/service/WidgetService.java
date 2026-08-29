package com.yagubogu.widget.service;

import com.yagubogu.game.domain.Game;
import com.yagubogu.game.repository.GameRepository;
import com.yagubogu.global.exception.BadRequestException;
import com.yagubogu.global.exception.ConflictException;
import com.yagubogu.global.exception.NotFoundException;
import com.yagubogu.member.domain.Member;
import com.yagubogu.member.repository.MemberRepository;
import com.yagubogu.widget.domain.WidgetDevice;
import com.yagubogu.widget.domain.WidgetLiveActivity;
import com.yagubogu.widget.domain.WidgetPlatform;
import com.yagubogu.widget.dto.v1.WidgetDeviceRegisterRequest;
import com.yagubogu.widget.dto.v1.WidgetLiveActivityRegisterRequest;
import com.yagubogu.widget.dto.v1.WidgetSettingsRequest;
import com.yagubogu.widget.dto.v1.WidgetSettingsResponse;
import com.yagubogu.widget.repository.WidgetDeviceRepository;
import com.yagubogu.widget.repository.WidgetLiveActivityRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class WidgetService {

    private final MemberRepository memberRepository;
    private final GameRepository gameRepository;
    private final WidgetDeviceRepository widgetDeviceRepository;
    private final WidgetLiveActivityRepository widgetLiveActivityRepository;

    @Transactional
    public void registerDevice(final long memberId, final WidgetDeviceRegisterRequest request) {
        Member member = getMember(memberId);
        String deviceId = normalizeDeviceId(request.deviceId());

        widgetDeviceRepository.findByDeviceId(deviceId)
                .ifPresentOrElse(
                        device -> updateDevice(device, member, request),
                        () -> widgetDeviceRepository.save(new WidgetDevice(
                                member,
                                request.platform(),
                                deviceId,
                                request.pushToken(),
                                request.appVersion()
                        ))
                );
    }

    private void updateDevice(
            final WidgetDevice device,
            final Member member,
            final WidgetDeviceRegisterRequest request
    ) {
        boolean ownerChanged = !device.belongsTo(member.getId());
        boolean changedToAndroid = device.isIos() && request.platform() == WidgetPlatform.ANDROID;

        if (ownerChanged || changedToAndroid) {
            widgetLiveActivityRepository.deleteAllByDevice(device);
        }
        device.updateRegistration(member, request.platform(), request.pushToken(), request.appVersion());
    }

    @Transactional
    public void registerLiveActivity(final long memberId, final WidgetLiveActivityRegisterRequest request) {
        WidgetDevice device = getOwnedDevice(memberId, request.deviceId());
        if (!device.isIos()) {
            throw new BadRequestException("Live Activity is available only on IOS devices");
        }

        Game game = gameRepository.findById(request.gameId())
                .orElseThrow(() -> new NotFoundException("Game is not found"));
        WidgetLiveActivity activity = widgetLiveActivityRepository.findByDeviceAndGame(device, game)
                .orElse(null);

        validateActivityIdOwner(request.activityId(), activity);
        if (activity == null) {
            widgetLiveActivityRepository.save(new WidgetLiveActivity(
                    device,
                    game,
                    request.activityId(),
                    request.updateToken()
            ));
            return;
        }
        activity.update(request.activityId(), request.updateToken());
    }

    private void validateActivityIdOwner(final String activityId, final WidgetLiveActivity targetActivity) {
        widgetLiveActivityRepository.findByActivityId(activityId)
                .filter(existing -> targetActivity == null || !existing.getId().equals(targetActivity.getId()))
                .ifPresent(existing -> {
                    throw new ConflictException("Activity ID already exists");
                });
    }

    @Transactional
    public void removeLiveActivity(final long memberId, final String activityId) {
        WidgetLiveActivity activity = widgetLiveActivityRepository
                .findByActivityIdAndDeviceMemberId(activityId, memberId)
                .orElseThrow(() -> new NotFoundException("Live Activity is not found"));

        widgetLiveActivityRepository.delete(activity);
    }

    @Transactional
    public void removeDevice(final long memberId, final String rawDeviceId) {
        WidgetDevice device = getOwnedDevice(memberId, rawDeviceId);

        widgetLiveActivityRepository.deleteAllByDevice(device);
        widgetDeviceRepository.delete(device);
    }

    public WidgetSettingsResponse findSettings(final long memberId, final String deviceId) {
        WidgetDevice device = getOwnedDevice(memberId, deviceId);

        return new WidgetSettingsResponse(device.isEnabled());
    }

    @Transactional
    public WidgetSettingsResponse updateSettings(
            final long memberId,
            final String deviceId,
            final WidgetSettingsRequest request
    ) {
        WidgetDevice device = getOwnedDevice(memberId, deviceId);
        device.updateEnabled(request.enabled());

        return new WidgetSettingsResponse(device.isEnabled());
    }

    private WidgetDevice getOwnedDevice(final long memberId, final String rawDeviceId) {
        String deviceId = normalizeDeviceId(rawDeviceId);

        return widgetDeviceRepository.findByDeviceIdAndMemberId(deviceId, memberId)
                .orElseThrow(() -> new NotFoundException("Widget device is not found"));
    }

    private Member getMember(final long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("Member is not found"));
    }

    private String normalizeDeviceId(final String deviceId) {
        try {
            return UUID.fromString(deviceId).toString();
        } catch (IllegalArgumentException exception) {
            throw new BadRequestException("deviceId must be a valid UUID");
        }
    }
}
