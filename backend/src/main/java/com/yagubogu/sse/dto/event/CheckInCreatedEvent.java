package com.yagubogu.sse.dto.event;

import java.time.LocalDate;

public record CheckInCreatedEvent(LocalDate date) {
}
