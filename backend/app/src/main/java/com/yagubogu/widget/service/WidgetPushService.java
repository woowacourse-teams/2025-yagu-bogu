package com.yagubogu.widget.service;

import com.yagubogu.widget.domain.WidgetDevice;
import com.yagubogu.widget.domain.WidgetLiveActivity;
import java.util.List;

/**
 * 플랫폼별(iOS APNs, Android FCM) 위젯 푸시 발송 인터페이스.
 *
 * <p>iOS: APNs HTTP/2 (apns-push-type: liveactivity)
 * - START: push-to-start 토큰(①)으로 새 Live Activity 생성
 * - UPDATE: 갱신 토큰(②)으로 content-state 갱신
 * - END: 갱신 토큰(②)으로 Live Activity 종료
 *
 * <p>Android: FCM HTTP v1 data 메시지 (notification 사용 금지)
 * - START/UPDATE/END: FCM 토큰으로 전달, type 필드로 구분
 */
public interface WidgetPushService {

    /**
     * 경기 시작 30분 전 위젯 START 푸시를 발송합니다.
     *
     * <p>iOS: push-to-start 토큰(①)으로 APNs 시작 이벤트 전송
     * <p>Android: FCM data 메시지 type=START 전송
     *
     * @param devices    대상 디바이스 목록
     * @param payload    초기 점수 페이로드 (홈팀/원정팀, gameState=SCHEDULED)
     */
    void sendStart(List<WidgetDevice> devices, LiveScorePayload payload);

    /**
     * 점수·이닝 변경 시 위젯 UPDATE 푸시를 발송합니다.
     *
     * <p>iOS: 갱신 토큰(②)으로 APNs update 이벤트 전송
     * <p>Android: FCM data 메시지 type=UPDATE 전송
     *
     * @param devices    Android 대상 디바이스 목록
     * @param activities iOS Live Activity 갱신 토큰 목록
     * @param payload    갱신된 점수 페이로드
     */
    void sendUpdate(List<WidgetDevice> devices, List<WidgetLiveActivity> activities,
                    LiveScorePayload payload);

    /**
     * 경기 종료(COMPLETED/CANCELED) 시 위젯 END 푸시를 발송합니다.
     *
     * <p>iOS: 갱신 토큰(②)으로 APNs end 이벤트 전송 → Live Activity 자동 소멸 예약
     * <p>Android: FCM data 메시지 type=END 전송 → 앱이 지속 알림 종료 처리
     *
     * @param devices    Android 대상 디바이스 목록
     * @param activities iOS Live Activity 갱신 토큰 목록
     * @param payload    최종 점수 페이로드
     */
    void sendEnd(List<WidgetDevice> devices, List<WidgetLiveActivity> activities,
                 LiveScorePayload payload);
}
