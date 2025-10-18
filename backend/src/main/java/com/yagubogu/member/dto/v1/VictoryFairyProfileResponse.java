package com.yagubogu.member.dto.v1;

import com.yagubogu.stat.dto.VictoryFairySummaryParam;

public record VictoryFairyProfileResponse(
        Integer ranking,
        Integer rankWithinTeam,
        Double score
) {

    public static VictoryFairyProfileResponse from(VictoryFairySummaryParam summary) {
        if (summary == null || summary.ranking() == null) {
            return empty();
        }

        return new VictoryFairyProfileResponse(
                summary.ranking(),
                summary.rankWithinTeam(),
                summary.score()
        );
    }

    private static VictoryFairyProfileResponse empty() {
        return new VictoryFairyProfileResponse(null, null, null);
    }
}
