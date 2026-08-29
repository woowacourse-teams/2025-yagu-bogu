package com.yagubogu.widget.dto.v1;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record WidgetLiveActivityRegisterRequest(
        @NotBlank String deviceId,
        @NotNull Long gameId,
        @NotBlank String activityId,
        @NotBlank String updateToken
) {
}
