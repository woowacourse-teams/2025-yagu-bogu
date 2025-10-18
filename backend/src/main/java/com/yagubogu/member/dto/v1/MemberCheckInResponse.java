package com.yagubogu.member.dto.v1;

import com.yagubogu.stat.dto.CheckInSummaryParam;
import java.time.LocalDate;

public record MemberCheckInResponse(
        int counts,
        String winRate,
        int winCounts,
        int drawCounts,
        int loseCounts,
        LocalDate recentCheckInDate
) {

    public static MemberCheckInResponse from(final CheckInSummaryParam summary) {
        if (summary == null) {
            return new MemberCheckInResponse(
                    0,
                    "0.0%",
                    0,
                    0,
                    0,
                    null
            );
        }
        String formattedWinRate = String.format("%.1f%%", summary.winRate());

        return new MemberCheckInResponse(
                summary.totalCount(),
                formattedWinRate,
                summary.winCounts(),
                summary.drawCounts(),
                summary.loseCounts(),
                summary.recentCheckInDate()
        );
    }
}
