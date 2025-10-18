package com.yagubogu.stat.dto;

import com.yagubogu.checkin.dto.StatCountsParam;
import java.time.LocalDate;

public record CheckInSummaryParam(
        int totalCount,
        double winRate,
        int winCounts,
        int drawCounts,
        int loseCounts,
        LocalDate recentCheckInDate
) {

    public static CheckInSummaryParam from(final StatCountsParam statCountsParam, final double winRate,
                                           final LocalDate date) {
        return new CheckInSummaryParam(
                statCountsParam.winCounts() + statCountsParam.drawCounts() + statCountsParam.loseCounts(),
                winRate,
                statCountsParam.winCounts(),
                statCountsParam.drawCounts(),
                statCountsParam.loseCounts(),
                date
        );
    }
}
