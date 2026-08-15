package com.yagubogu.widget.dto.v1;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record WidgetLiveActivityRegisterRequest(
        @NotBlank
        @Pattern(
                regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$",
                message = "deviceId must be a valid UUID"
        )
        String deviceId,
        @NotNull @Positive Long gameId,
        @NotBlank @Size(max = 255) String activityId,
        @NotBlank @Size(max = 4096) String updateToken
) {
}
