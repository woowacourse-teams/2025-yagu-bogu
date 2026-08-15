package com.yagubogu.widget.dto.v1;

import com.yagubogu.widget.domain.WidgetPlatform;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record WidgetDeviceRegisterRequest(
        @NotNull WidgetPlatform platform,
        @NotBlank
        @Pattern(
                regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$",
                message = "deviceId must be a valid UUID"
        )
        String deviceId,
        @NotBlank
        @Size(max = 4096)
        String pushToken,
        @Size(max = 50)
        String appVersion
) {
}
