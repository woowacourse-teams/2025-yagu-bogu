package com.yagubogu.stat.dto;

import com.yagubogu.checkin.dto.VictoryFairyRankParam;

public record VictoryFairySummaryParam(
        Integer ranking,
        Integer rankWithinTeam,
        Double score
) {

    public static VictoryFairySummaryParam from(
            final VictoryFairyRankParam overallRankInfo,
            final Integer rankWithinTeam) {
        if (overallRankInfo == null) {
            return empty();
        }

        return new VictoryFairySummaryParam(
                overallRankInfo.rank(),
                rankWithinTeam,
                overallRankInfo.score()
        );
    }

    public static VictoryFairySummaryParam empty() {
        return new VictoryFairySummaryParam(null, null, null);
    }
}
