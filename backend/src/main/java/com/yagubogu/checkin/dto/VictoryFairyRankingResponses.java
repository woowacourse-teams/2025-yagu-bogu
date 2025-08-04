package com.yagubogu.checkin.dto;

import java.util.ArrayList;
import java.util.List;

public record VictoryFairyRankingResponses(
        List<VictoryFairyRankingResponse> topRankings,
        VictoryFairyRankingResponse myRanking
) {

    public record VictoryFairyRankingResponse(
            int ranking,
            String nickname,
            String teamShortName,
            double winPercent
    ) {
    }

    public static VictoryFairyRankingResponses from(
            List<VictoryFairyRankingEntryResponse> topRankings,
            VictoryFairyRankingEntryResponse myRankingData,
            int myRanking
    ) {

        List<VictoryFairyRankingResponse> rankingResponses = new ArrayList<>();
        for (int i = 0; i < topRankings.size(); i++) {
            VictoryFairyRankingEntryResponse data = topRankings.get(i);
            rankingResponses.add(new VictoryFairyRankingResponse(
                    i + 1,
                    data.nickname(),
                    data.teamShortName(),
                    data.winPercent()
            ));
        }

        VictoryFairyRankingResponse myRankingResponse = null;
        if (myRankingData != null) {
            myRankingResponse = new VictoryFairyRankingResponse(
                    myRanking,
                    myRankingData.nickname(),
                    myRankingData.teamShortName(),
                    myRankingData.winPercent()
            );
        }

        return new VictoryFairyRankingResponses(rankingResponses, myRankingResponse);
    }
}
