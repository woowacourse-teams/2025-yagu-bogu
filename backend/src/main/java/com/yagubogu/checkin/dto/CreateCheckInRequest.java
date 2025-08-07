package com.yagubogu.checkin.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;

public record CreateCheckInRequest(
        long stadiumId,
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date
) {
}
