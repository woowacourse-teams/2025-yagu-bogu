package com.yagubogu.checkin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record CreatePastCheckInRequest(
        @Schema(description = "경기 ID", example = "1")
        @NotNull(message = "경기 ID는 필수입니다.")
        Long gameId
) {
}
