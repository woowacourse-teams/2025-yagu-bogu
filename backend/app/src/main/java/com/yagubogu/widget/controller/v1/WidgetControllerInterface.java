package com.yagubogu.widget.controller.v1;

import com.yagubogu.auth.dto.MemberClaims;
import com.yagubogu.widget.dto.v1.WidgetDeviceRegisterRequest;
import com.yagubogu.widget.dto.v1.WidgetLiveActivityRegisterRequest;
import com.yagubogu.widget.dto.v1.WidgetSettingsRequest;
import com.yagubogu.widget.dto.v1.WidgetSettingsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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

    @Operation(summary = "디바이스 푸시 토큰 등록 또는 갱신")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "등록 또는 갱신 성공"),
            @ApiResponse(responseCode = "400", description = "요청값 오류"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PostMapping("/devices")
    ResponseEntity<Void> registerDevice(
            @Parameter(hidden = true) MemberClaims memberClaims,
            @Valid @RequestBody WidgetDeviceRegisterRequest request
    );

    @Operation(summary = "iOS Live Activity 갱신 토큰 등록 또는 갱신")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "등록 또는 갱신 성공"),
            @ApiResponse(responseCode = "400", description = "요청값 또는 플랫폼 오류"),
            @ApiResponse(responseCode = "404", description = "디바이스 또는 경기를 찾을 수 없음"),
            @ApiResponse(responseCode = "409", description = "이미 사용 중인 activityId")
    })
    @PostMapping("/live-activities")
    ResponseEntity<Void> registerLiveActivity(
            @Parameter(hidden = true) MemberClaims memberClaims,
            @Valid @RequestBody WidgetLiveActivityRegisterRequest request
    );

    @Operation(summary = "iOS Live Activity 갱신 토큰 해제")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "해제 성공"),
            @ApiResponse(responseCode = "404", description = "활동을 찾을 수 없음")
    })
    @DeleteMapping("/live-activities/{activityId}")
    ResponseEntity<Void> removeLiveActivity(
            @Parameter(hidden = true) MemberClaims memberClaims,
            @PathVariable String activityId
    );

    @Operation(summary = "디바이스 푸시 토큰 해제")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "해제 성공"),
            @ApiResponse(responseCode = "400", description = "deviceId 형식 오류"),
            @ApiResponse(responseCode = "404", description = "디바이스를 찾을 수 없음")
    })
    @DeleteMapping("/devices/{deviceId}")
    ResponseEntity<Void> removeDevice(
            @Parameter(hidden = true) MemberClaims memberClaims,
            @PathVariable String deviceId
    );

    @Operation(summary = "디바이스별 위젯 사용 설정 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "설정 조회 성공"),
            @ApiResponse(responseCode = "400", description = "deviceId 형식 오류"),
            @ApiResponse(responseCode = "404", description = "디바이스를 찾을 수 없음")
    })
    @GetMapping("/devices/{deviceId}/settings")
    ResponseEntity<WidgetSettingsResponse> findSettings(
            @Parameter(hidden = true) MemberClaims memberClaims,
            @PathVariable String deviceId
    );

    @Operation(summary = "디바이스별 위젯 사용 설정 변경")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "설정 변경 성공"),
            @ApiResponse(responseCode = "400", description = "요청값 또는 deviceId 형식 오류"),
            @ApiResponse(responseCode = "404", description = "디바이스를 찾을 수 없음")
    })
    @PatchMapping("/devices/{deviceId}/settings")
    ResponseEntity<WidgetSettingsResponse> updateSettings(
            @Parameter(hidden = true) MemberClaims memberClaims,
            @PathVariable String deviceId,
            @Valid @RequestBody WidgetSettingsRequest request
    );
}
