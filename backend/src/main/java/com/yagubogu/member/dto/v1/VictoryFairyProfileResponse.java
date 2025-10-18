package com.yagubogu.member.dto.v1;

import com.yagubogu.stat.dto.VictoryFairySummaryParam;

public record VictoryFairyProfileResponse(
        int ranking,
        int rankWithinTeam,
        double score
) {

    public static VictoryFairyProfileResponse from(VictoryFairySummaryParam victoryFairySummaryParam) {
        return new VictoryFairyProfileResponse(
                victoryFairySummaryParam.ranking(),
                victoryFairySummaryParam.rankWithinTeam(),
                victoryFairySummaryParam.score()
        );
    }
}
