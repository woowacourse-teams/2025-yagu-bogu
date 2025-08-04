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
            List<VictoryFairyRankingDataResponse> top5,
            VictoryFairyRankingDataResponse myRankingData,
            int myRanking
    ) {

        List<VictoryFairyRankingResponse> topRankings = new ArrayList<>();
        for (int i = 0; i < top5.size(); i++) {
            VictoryFairyRankingDataResponse data = top5.get(i);
            topRankings.add(new VictoryFairyRankingResponse(
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

        return new VictoryFairyRankingResponses(topRankings, myRankingResponse);
    }
}
