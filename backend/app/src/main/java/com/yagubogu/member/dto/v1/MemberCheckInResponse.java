package com.yagubogu.member.dto.v1;

import com.yagubogu.stat.dto.CheckInSummaryParam;
import java.time.LocalDate;

public record MemberCheckInResponse(
        Integer counts,
        Double winRate,
        Integer winCounts,
        Integer drawCounts,
        Integer loseCounts,
        LocalDate recentCheckInDate
) {

    public static MemberCheckInResponse from(final CheckInSummaryParam summary) {
        if (summary == null || summary.totalCount() == 0) {
            return empty();
        }

        return new MemberCheckInResponse(
                summary.totalCount(),
                summary.winRate(),
                summary.winCounts(),
                summary.drawCounts(),
                summary.loseCounts(),
                summary.recentCheckInDate()
        );
    }

    private static MemberCheckInResponse empty() {
        return new MemberCheckInResponse(null, null, null, null, null, null);
    }
}
