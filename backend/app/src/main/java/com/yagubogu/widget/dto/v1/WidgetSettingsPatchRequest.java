package com.yagubogu.widget.dto.v1;

import jakarta.validation.constraints.NotNull;

public record WidgetSettingsPatchRequest(@NotNull Boolean enabled) {
}
