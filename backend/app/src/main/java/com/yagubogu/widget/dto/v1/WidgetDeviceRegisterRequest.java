package com.yagubogu.widget.dto.v1;

import com.yagubogu.widget.domain.WidgetPlatform;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record WidgetDeviceRegisterRequest(
        @NotNull WidgetPlatform platform,
        @NotBlank String deviceId,
        @NotBlank String pushToken,
        String appVersion
) {
}
