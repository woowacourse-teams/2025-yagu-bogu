package com.yagubogu.checkin.dto;

import java.util.List;

public record CheckInHistoryResponse(
        List<CheckInGameResponse> checkInHistory
) {
}
