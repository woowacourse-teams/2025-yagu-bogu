package com.yagubogu.member.dto;

import com.yagubogu.stat.dto.CheckInSummary;

public record MemberCheckInResponse(
        int counts,
        String winRate
) {

    public static MemberCheckInResponse from(final CheckInSummary summary) {
        if (summary == null) {
            return new MemberCheckInResponse(0, "0.0%");
        }
        String formattedWinRate = String.format("%.1f%%", summary.winRate());

        return new MemberCheckInResponse(summary.totalCount(), formattedWinRate);
    }
}
