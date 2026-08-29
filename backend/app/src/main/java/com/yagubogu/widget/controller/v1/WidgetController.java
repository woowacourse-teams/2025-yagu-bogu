package com.yagubogu.widget.controller.v1;

import com.yagubogu.auth.annotation.RequireRole;
import com.yagubogu.auth.dto.MemberClaims;
import com.yagubogu.widget.dto.v1.WidgetDeviceRegisterRequest;
import com.yagubogu.widget.dto.v1.WidgetLiveActivityRegisterRequest;
import com.yagubogu.widget.dto.v1.WidgetSettingsRequest;
import com.yagubogu.widget.dto.v1.WidgetSettingsResponse;
import com.yagubogu.widget.service.WidgetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequireRole
@RestController
public class WidgetController implements WidgetControllerInterface {

    private final WidgetService widgetService;

    @Override
    public ResponseEntity<Void> registerDevice(
            final MemberClaims memberClaims,
            @Valid @RequestBody final WidgetDeviceRegisterRequest request
    ) {
        widgetService.registerDevice(memberClaims.id(), request);

        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> registerLiveActivity(
            final MemberClaims memberClaims,
            @Valid @RequestBody final WidgetLiveActivityRegisterRequest request
    ) {
        widgetService.registerLiveActivity(memberClaims.id(), request);

        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> removeLiveActivity(
            final MemberClaims memberClaims,
            @PathVariable final String activityId
    ) {
        widgetService.removeLiveActivity(memberClaims.id(), activityId);

        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> removeDevice(
            final MemberClaims memberClaims,
            @PathVariable final String deviceId
    ) {
        widgetService.removeDevice(memberClaims.id(), deviceId);

        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<WidgetSettingsResponse> findSettings(
            final MemberClaims memberClaims,
            @PathVariable final String deviceId
    ) {
        WidgetSettingsResponse response = widgetService.findSettings(memberClaims.id(), deviceId);

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<WidgetSettingsResponse> updateSettings(
            final MemberClaims memberClaims,
            @PathVariable final String deviceId,
            @Valid @RequestBody final WidgetSettingsRequest request
    ) {
        WidgetSettingsResponse response = widgetService.updateSettings(memberClaims.id(), deviceId, request);

        return ResponseEntity.ok(response);
    }
}
