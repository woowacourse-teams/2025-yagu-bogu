package com.yagubogu.widget.controller.v1;

import com.yagubogu.auth.dto.MemberClaims;
import com.yagubogu.widget.dto.v1.WidgetDeviceRegisterRequest;
import com.yagubogu.widget.dto.v1.WidgetLiveActivityRegisterRequest;
import com.yagubogu.widget.dto.v1.WidgetSettingsPatchRequest;
import com.yagubogu.widget.dto.v1.WidgetSettingsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "Widget", description = "잠금화면 실시간 스코어 위젯 API")
@RequestMapping("/widgets")
public interface WidgetControllerInterface {

    @Operation(
            summary = "[API 1] 디바이스 푸시 토큰 등록/갱신",
            description = """
                    앱이 위젯 푸시를 받을 디바이스 토큰을 등록합니다.
                    (memberId, deviceId) 기준 upsert — 토큰이 바뀔 때마다 재호출하세요.
                    iOS: push-to-start 토큰(①), Android: FCM 등록 토큰.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "등록/갱신 성공"),
            @ApiResponse(responseCode = "400", description = "필수 필드 누락 또는 platform 값 오류"),
            @ApiResponse(responseCode = "401", description = "인증 토큰 없음/만료"),
            @ApiResponse(responseCode = "404", description = "회원 없음")
    })
    @PostMapping("/devices")
    ResponseEntity<Void> registerDevice(
            @Parameter(hidden = true) MemberClaims memberClaims,
            @RequestBody WidgetDeviceRegisterRequest request
    );

    @Operation(
            summary = "[API 3] 디바이스 토큰 해제",
            description = "로그아웃·앱 삭제 직전 등에 호출해 주소록에서 디바이스를 제거합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "해제 성공"),
            @ApiResponse(responseCode = "401", description = "인증 토큰 없음/만료"),
            @ApiResponse(responseCode = "404", description = "등록된 디바이스 없음")
    })
    @DeleteMapping("/devices/{deviceId}")
    ResponseEntity<Void> unregisterDevice(
            @Parameter(hidden = true) MemberClaims memberClaims,
            @PathVariable String deviceId
    );

    @Operation(
            summary = "[API 2] iOS Live Activity 갱신 토큰 등록 (iOS 전용)",
            description = """
                    위젯이 켜진 직후 iOS ActivityKit이 발급한 갱신 토큰(②)을 등록합니다.
                    같은 activityId로 재호출 시 updateToken만 갱신(토큰 회전 대응).
                    Android는 이 API를 호출하지 않습니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "등록 성공"),
            @ApiResponse(responseCode = "400", description = "필수 필드 누락"),
            @ApiResponse(responseCode = "401", description = "인증 토큰 없음/만료"),
            @ApiResponse(responseCode = "404", description = "등록된 디바이스 또는 경기 없음")
    })
    @PostMapping("/live-activities")
    ResponseEntity<Void> registerLiveActivity(
            @Parameter(hidden = true) MemberClaims memberClaims,
            @RequestBody WidgetLiveActivityRegisterRequest request
    );

    @Operation(
            summary = "[API 2] iOS Live Activity 해제 (iOS 전용)",
            description = "활동이 종료되거나 사용자가 위젯을 제거했을 때 호출합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "해제 성공"),
            @ApiResponse(responseCode = "401", description = "인증 토큰 없음/만료"),
            @ApiResponse(responseCode = "404", description = "등록된 Live Activity 없음")
    })
    @DeleteMapping("/live-activities/{activityId}")
    ResponseEntity<Void> unregisterLiveActivity(
            @Parameter(hidden = true) MemberClaims memberClaims,
            @PathVariable String activityId
    );

    @Operation(
            summary = "[API 4] 위젯 설정 조회",
            description = "위젯 활성화 여부를 조회합니다. 설정이 없으면 기본값 true를 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 토큰 없음/만료")
    })
    @GetMapping("/settings")
    ResponseEntity<WidgetSettingsResponse> getSettings(
            @Parameter(hidden = true) MemberClaims memberClaims
    );

    @Operation(
            summary = "[API 4] 위젯 설정 변경",
            description = "위젯 활성화 여부를 변경합니다. enabled=false이면 스케줄러가 이 계정의 기기를 제외합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "변경 성공"),
            @ApiResponse(responseCode = "400", description = "필수 필드 누락"),
            @ApiResponse(responseCode = "401", description = "인증 토큰 없음/만료")
    })
    @PatchMapping("/settings")
    ResponseEntity<Void> updateSettings(
            @Parameter(hidden = true) MemberClaims memberClaims,
            @RequestBody WidgetSettingsPatchRequest request
    );
}
