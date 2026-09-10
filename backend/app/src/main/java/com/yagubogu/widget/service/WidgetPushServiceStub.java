package com.yagubogu.widget.service;

import com.yagubogu.widget.domain.WidgetDevice;
import com.yagubogu.widget.domain.WidgetLiveActivity;
import com.yagubogu.widget.domain.WidgetPlatform;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * WidgetPushService 스텁 구현체.
 *
 * <p>실제 구현 시 아래 라이브러리/설정이 필요합니다.
 * <ul>
 *   <li>iOS APNs: {@code com.eatthepath:pushy} 또는 자체 APNs HTTP/2 클라이언트
 *       + Apple Developer Console에서 발급한 .p8 키(Key ID, Team ID)</li>
 *   <li>Android FCM: {@code com.google.firebase:firebase-admin} SDK
 *       + Firebase 서비스 계정 JSON 키</li>
 * </ul>
 *
 * <p>환경변수 예시:
 * <pre>
 *   APNS_KEY_ID, APNS_TEAM_ID, APNS_PRIVATE_KEY (Base64 encoded .p8)
 *   FIREBASE_CREDENTIALS_PATH (서비스 계정 JSON 파일 경로 또는 내용)
 * </pre>
 */
@Slf4j
@Service
public class WidgetPushServiceStub implements WidgetPushService {

    @Override
    public void sendStart(final List<WidgetDevice> devices, final LiveScorePayload payload) {
        for (final WidgetDevice device : devices) {
            if (device.getPlatform() == WidgetPlatform.IOS) {
                log.info("[WIDGET-PUSH] APNs START → deviceId={} token={} gameId={}",
                        device.getDeviceId(), masked(device.getPushToken()), payload.gameId());
                // TODO: APNs HTTP/2 POST to api.push.apple.com/3/device/{pushToken}
                // apns-push-type: liveactivity
                // apns-topic: com.yagubogu.push-type.liveactivity
                // Body: { "aps": { "event": "start", "attributes-type": "YaguBoguWidgetAttributes",
                //         "attributes": { gameId, homeTeam, awayTeam, myTeamCode },
                //         "content-state": { homeScore, awayScore, inning, inningHalf, gameState } } }
            } else {
                log.info("[WIDGET-PUSH] FCM START → deviceId={} token={} gameId={}",
                        device.getDeviceId(), masked(device.getPushToken()), payload.gameId());
                // TODO: FCM HTTP v1 POST to fcm.googleapis.com/v1/projects/{projectId}/messages:send
                // data: { type=START, gameId, homeTeamCode, homeTeamName, awayTeamCode, awayTeamName,
                //         myTeamCode, homeScore, awayScore, inning, inningHalf, gameState }
                // android.priority: high
            }
        }
    }

    @Override
    public void sendUpdate(final List<WidgetDevice> devices,
                           final List<WidgetLiveActivity> activities,
                           final LiveScorePayload payload) {
        for (final WidgetLiveActivity activity : activities) {
            log.info("[WIDGET-PUSH] APNs UPDATE → activityId={} token={} gameId={}",
                    activity.getActivityId(), masked(activity.getUpdateToken()), payload.gameId());
            // TODO: APNs HTTP/2 POST to api.push.apple.com/3/device/{updateToken}
            // Body: { "aps": { "event": "update", "content-state": { ... } } }
        }
        for (final WidgetDevice device : devices) {
            if (device.getPlatform() == WidgetPlatform.ANDROID) {
                log.info("[WIDGET-PUSH] FCM UPDATE → deviceId={} gameId={}",
                        device.getDeviceId(), payload.gameId());
                // TODO: FCM data message type=UPDATE
            }
        }
    }

    @Override
    public void sendEnd(final List<WidgetDevice> devices,
                        final List<WidgetLiveActivity> activities,
                        final LiveScorePayload payload) {
        for (final WidgetLiveActivity activity : activities) {
            log.info("[WIDGET-PUSH] APNs END → activityId={} token={} gameId={}",
                    activity.getActivityId(), masked(activity.getUpdateToken()), payload.gameId());
            // TODO: APNs HTTP/2 POST with event=end, dismissal-date=gameEnd+15min
        }
        for (final WidgetDevice device : devices) {
            if (device.getPlatform() == WidgetPlatform.ANDROID) {
                log.info("[WIDGET-PUSH] FCM END → deviceId={} gameId={}",
                        device.getDeviceId(), payload.gameId());
                // TODO: FCM data message type=END → 앱이 setOngoing(false) 후 알림 정리
            }
        }
    }

    private String masked(final String token) {
        if (token == null || token.length() < 8) {
            return "****";
        }
        return token.substring(0, 6) + "..." + token.substring(token.length() - 4);
    }
}
