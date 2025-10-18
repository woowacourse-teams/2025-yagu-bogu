package com.yagubogu.stat.dto;

import com.yagubogu.checkin.dto.StatCountsParam;
import java.time.LocalDate;

public record CheckInSummary(
        int totalCount,
        double winRate,
        int winCounts,
        int drawCounts,
        int loseCounts,
        LocalDate recentCheckInDate
) {

    public static CheckInSummary from(final StatCountsParam statCountsParam, final double winRate,
                                      final LocalDate date) {
        return new CheckInSummary(
                statCountsParam.winCounts() + statCountsParam.drawCounts() + statCountsParam.loseCounts(),
                winRate,
                statCountsParam.winCounts(),
                statCountsParam.drawCounts(),
                statCountsParam.loseCounts(),
                date
        );
    }
}
