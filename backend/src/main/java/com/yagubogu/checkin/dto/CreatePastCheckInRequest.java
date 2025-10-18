package com.yagubogu.checkin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record CreatePastCheckInRequest(
        @Schema(description = "경기 ID", example = "1")
        @NotNull(message = "경기 ID는 필수입니다.")
        Long gameId,

        @Schema(description = "경기 날짜", example = "2024-05-15")
        @NotNull(message = "경기 날짜는 필수입니다.")
        LocalDate date
) {
}
