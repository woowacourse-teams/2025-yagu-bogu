package com.yagubogu.stat.dto;

import com.yagubogu.checkin.dto.VictoryFairyRankParam;

public record VictoryFairySummaryParam(
        int ranking,
        int rankWithinTeam,
        double score
) {

    public static VictoryFairySummaryParam empty() {
        return new VictoryFairySummaryParam(0, 0, 0.0);
    }

    public static VictoryFairySummaryParam from(final VictoryFairyRankParam overallRankInfo,
                                                final int rankWithinTeam) {
        return new VictoryFairySummaryParam(
                overallRankInfo.rank(),
                rankWithinTeam,
                overallRankInfo.score()
        );
    }
}
