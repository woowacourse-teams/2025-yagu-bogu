package com.yagubogu.widget.service;

import com.yagubogu.game.domain.Game;
import com.yagubogu.game.repository.GameRepository;
import com.yagubogu.global.exception.NotFoundException;
import com.yagubogu.member.domain.Member;
import com.yagubogu.member.repository.MemberRepository;
import com.yagubogu.widget.domain.WidgetDevice;
import com.yagubogu.widget.domain.WidgetGamePush;
import com.yagubogu.widget.domain.WidgetLiveActivity;
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
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WidgetService {

    private final WidgetDeviceRepository widgetDeviceRepository;
    private final WidgetLiveActivityRepository widgetLiveActivityRepository;
    private final WidgetSettingsRepository widgetSettingsRepository;
    private final WidgetGamePushRepository widgetGamePushRepository;
    private final MemberRepository memberRepository;
    private final GameRepository gameRepository;
    private final WidgetPushService widgetPushService;
    private final Clock clock;

    // ─── API 1: 디바이스 토큰 등록/갱신 ──────────────────────────────────────

    /**
     * 디바이스 푸시 토큰을 upsert합니다. (memberId, deviceId) 기준.
     * 같은 deviceId로 재호출 시 토큰 갱신.
     */
    @Transactional
    public void registerDevice(final Long memberId, final WidgetDeviceRegisterRequest request) {
        final Member member = findMember(memberId);

        widgetDeviceRepository.findById(request.deviceId())
                .ifPresentOrElse(
                        existing -> existing.update(member, request.platform(),
                                request.pushToken(), request.appVersion()),
                        () -> widgetDeviceRepository.save(
                                new WidgetDevice(request.deviceId(), member,
                                        request.platform(), request.pushToken(),
                                        request.appVersion()))
                );

        log.info("[WIDGET] Device registered: memberId={} deviceId={} platform={}",
                memberId, request.deviceId(), request.platform());
    }

    // ─── API 3: 디바이스 토큰 해제 ───────────────────────────────────────────

    @Transactional
    public void unregisterDevice(final Long memberId, final String deviceId) {
        final WidgetDevice device = widgetDeviceRepository.findByDeviceIdAndMemberId(deviceId, memberId)
                .orElseThrow(() -> new NotFoundException("등록된 디바이스가 없습니다."));

        widgetLiveActivityRepository.findAllByDeviceDeviceId(deviceId)
                .forEach(widgetLiveActivityRepository::delete);

        widgetDeviceRepository.delete(device);
        log.info("[WIDGET] Device unregistered: memberId={} deviceId={}", memberId, deviceId);
    }

    // ─── API 2: iOS Live Activity 갱신 토큰 등록 ─────────────────────────────

    /**
     * iOS Live Activity 갱신 토큰을 등록합니다.
     * 같은 activityId로 재호출 시 updateToken만 갱신(토큰 회전 대응).
     */
    @Transactional
    public void registerLiveActivity(final Long memberId,
                                     final WidgetLiveActivityRegisterRequest request) {
        final WidgetDevice device = widgetDeviceRepository
                .findByDeviceIdAndMemberId(request.deviceId(), memberId)
                .orElseThrow(() -> new NotFoundException("등록된 디바이스가 없습니다."));

        final Game game = gameRepository.findById(request.gameId())
                .orElseThrow(() -> new NotFoundException("경기를 찾을 수 없습니다."));

        widgetLiveActivityRepository.findById(request.activityId())
                .ifPresentOrElse(
                        existing -> existing.updateToken(request.updateToken()),
                        () -> widgetLiveActivityRepository.save(
                                new WidgetLiveActivity(request.activityId(), device,
                                        game, request.updateToken()))
                );

        log.info("[WIDGET] LiveActivity registered: memberId={} activityId={} gameId={}",
                memberId, request.activityId(), request.gameId());
    }

    // ─── API 2: iOS Live Activity 해제 ───────────────────────────────────────

    @Transactional
    public void unregisterLiveActivity(final Long memberId, final String activityId) {
        final WidgetLiveActivity activity = widgetLiveActivityRepository.findById(activityId)
                .orElseThrow(() -> new NotFoundException("등록된 Live Activity가 없습니다."));

        if (!activity.getDevice().getMember().isSameId(memberId)) {
            throw new NotFoundException("등록된 Live Activity가 없습니다.");
        }

        widgetLiveActivityRepository.delete(activity);
        log.info("[WIDGET] LiveActivity unregistered: memberId={} activityId={}", memberId, activityId);
    }

    // ─── API 4: 위젯 설정 조회/변경 ──────────────────────────────────────────

    @Transactional(readOnly = true)
    public WidgetSettingsResponse getSettings(final Long memberId) {
        final boolean enabled = widgetSettingsRepository.findByMemberId(memberId)
                .map(WidgetSettings::isEnabled)
                .orElse(true); // 설정 row 없으면 기본값 true
        return new WidgetSettingsResponse(enabled);
    }

    @Transactional
    public void updateSettings(final Long memberId, final WidgetSettingsPatchRequest request) {
        widgetSettingsRepository.findByMemberId(memberId)
                .ifPresentOrElse(
                        settings -> settings.setEnabled(request.enabled()),
                        () -> widgetSettingsRepository.save(
                                new WidgetSettings(memberId, request.enabled()))
                );
    }

    // ─── 스케줄러에서 호출: START 푸시 ───────────────────────────────────────

    /**
     * 경기 시작 30분 전 START 푸시를 발송합니다.
     * 더블헤더 안전장치: iOS 기기에 이미 활성 Live Activity가 있으면 START를 건너뜁니다.
     */
    @Transactional
    public void sendStartPush(final Game game) {
        final WidgetGamePush pushLog = widgetGamePushRepository.findById(game.getId())
                .orElseGet(() -> widgetGamePushRepository.save(new WidgetGamePush(game.getId())));

        if (pushLog.isStartSent()) {
            log.debug("[WIDGET] START already sent for gameId={}, skipping", game.getId());
            return;
        }

        final List<WidgetDevice> allDevices = widgetDeviceRepository
                .findDevicesForGame(game.getHomeTeam(), game.getAwayTeam());

        // iOS: 이미 활성 Live Activity가 있는 기기는 START 제외 (더블헤더 안전장치)
        final List<WidgetDevice> iosDevicesWithoutActivity = allDevices.stream()
                .filter(d -> d.getPlatform().name().equals("IOS"))
                .filter(d -> !widgetLiveActivityRepository.existsByDeviceDeviceId(d.getDeviceId()))
                .toList();

        final List<WidgetDevice> androidDevices = allDevices.stream()
                .filter(d -> d.getPlatform().name().equals("ANDROID"))
                .toList();

        final List<WidgetDevice> targets = concatLists(iosDevicesWithoutActivity, androidDevices);

        if (targets.isEmpty()) {
            log.info("[WIDGET] No eligible devices for START push gameId={}", game.getId());
            pushLog.markStartSent(LocalDateTime.now(clock));
            return;
        }

        // 기기별 myTeamCode: 홈팀/원정팀 중 멤버 응원팀에 맞는 코드
        // 단순화: payload는 홈팀 코드 기준으로 전달하고 앱이 자기 팀을 구분
        // (실제로는 기기별 멤버 응원팀을 넣어야 하나 배치 발송이므로 앱에서 판별)
        final LiveScorePayload payload = LiveScorePayload.forStart(game, null);

        widgetPushService.sendStart(targets, payload);
        pushLog.markStartSent(LocalDateTime.now(clock));

        log.info("[WIDGET] START push sent: gameId={} targets={}", game.getId(), targets.size());
    }

    // ─── 스케줄러에서 호출: END 푸시 ─────────────────────────────────────────

    @Transactional
    public void sendEndPush(final Game game) {
        final WidgetGamePush pushLog = widgetGamePushRepository.findById(game.getId())
                .orElse(null);

        if (pushLog == null || !pushLog.isStartSent()) {
            log.debug("[WIDGET] START not sent for gameId={}, skipping END", game.getId());
            return;
        }

        if (pushLog.isEndSent()) {
            log.debug("[WIDGET] END already sent for gameId={}, skipping", game.getId());
            return;
        }

        final List<WidgetDevice> androidDevices = widgetDeviceRepository
                .findDevicesForGame(game.getHomeTeam(), game.getAwayTeam())
                .stream()
                .filter(d -> d.getPlatform().name().equals("ANDROID"))
                .toList();

        final List<WidgetLiveActivity> activities = widgetLiveActivityRepository
                .findAllByGameId(game.getId());

        final LiveScorePayload payload = LiveScorePayload.fromGame(game, null);

        widgetPushService.sendEnd(androidDevices, activities, payload);

        // iOS: Live Activity 종료 후 DB에서 정리
        activities.forEach(widgetLiveActivityRepository::delete);

        pushLog.markEndSent(LocalDateTime.now(clock));
        log.info("[WIDGET] END push sent: gameId={}", game.getId());
    }

    private Member findMember(final Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("회원을 찾을 수 없습니다."));
    }

    private static <T> List<T> concatLists(final List<T> a, final List<T> b) {
        return java.util.stream.Stream.concat(a.stream(), b.stream()).toList();
    }
}
