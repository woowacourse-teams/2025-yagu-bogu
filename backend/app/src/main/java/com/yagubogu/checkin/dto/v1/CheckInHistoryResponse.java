package com.yagubogu.checkin.dto.v1;

import com.yagubogu.checkin.dto.CheckInGameParam;
import java.util.List;

public record CheckInHistoryResponse(
        List<CheckInGameParam> checkInHistory
) {
}
