package com.yagubogu.widget.controller.v1;

import com.yagubogu.auth.annotation.RequireRole;
import com.yagubogu.auth.dto.MemberClaims;
import com.yagubogu.widget.dto.v1.WidgetDeviceRegisterRequest;
import com.yagubogu.widget.dto.v1.WidgetLiveActivityRegisterRequest;
import com.yagubogu.widget.dto.v1.WidgetSettingsPatchRequest;
import com.yagubogu.widget.dto.v1.WidgetSettingsResponse;
import com.yagubogu.widget.service.WidgetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class WidgetController implements WidgetControllerInterface {

    private final WidgetService widgetService;

    @RequireRole
    @Override
    public ResponseEntity<Void> registerDevice(
            final MemberClaims memberClaims,
            @Valid @RequestBody final WidgetDeviceRegisterRequest request
    ) {
        widgetService.registerDevice(memberClaims.id(), request);
        return ResponseEntity.noContent().build();
    }

    @RequireRole
    @Override
    public ResponseEntity<Void> unregisterDevice(
            final MemberClaims memberClaims,
            @PathVariable final String deviceId
    ) {
        widgetService.unregisterDevice(memberClaims.id(), deviceId);
        return ResponseEntity.noContent().build();
    }

    @RequireRole
    @Override
    public ResponseEntity<Void> registerLiveActivity(
            final MemberClaims memberClaims,
            @Valid @RequestBody final WidgetLiveActivityRegisterRequest request
    ) {
        widgetService.registerLiveActivity(memberClaims.id(), request);
        return ResponseEntity.noContent().build();
    }

    @RequireRole
    @Override
    public ResponseEntity<Void> unregisterLiveActivity(
            final MemberClaims memberClaims,
            @PathVariable final String activityId
    ) {
        widgetService.unregisterLiveActivity(memberClaims.id(), activityId);
        return ResponseEntity.noContent().build();
    }

    @RequireRole
    @Override
    public ResponseEntity<WidgetSettingsResponse> getSettings(final MemberClaims memberClaims) {
        return ResponseEntity.ok(widgetService.getSettings(memberClaims.id()));
    }

    @RequireRole
    @Override
    public ResponseEntity<Void> updateSettings(
            final MemberClaims memberClaims,
            @Valid @RequestBody final WidgetSettingsPatchRequest request
    ) {
        widgetService.updateSettings(memberClaims.id(), request);
        return ResponseEntity.noContent().build();
    }
}
