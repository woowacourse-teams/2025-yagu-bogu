package com.yagubogu.checkin.dto;

import java.util.ArrayList;
import java.util.List;

public record VictoryFairyRackingResponses(
        List<VictoryFairyRackingResponse> topRankings,
        VictoryFairyRackingResponse myRanking
) {

    public record VictoryFairyRackingResponse(
            int ranking,
            String nickname,
            String teamShortName,
            double winPercent
    ) {
    }

    public static VictoryFairyRackingResponses from(
            List<VictoryFairyRankingDataResponse> top5,
            VictoryFairyRankingDataResponse myRankingData,
            int myRanking
    ) {

        List<VictoryFairyRackingResponse> topRankings = new ArrayList<>();
        for (int i = 0; i < top5.size(); i++) {
            VictoryFairyRankingDataResponse data = top5.get(i);
            topRankings.add(new VictoryFairyRackingResponse(
                    i + 1,
                    data.nickname(),
                    data.teamShortName(),
                    data.winPercent()
            ));
        }

        VictoryFairyRackingResponse myRankingResponse = null;
        if (myRankingData != null) {
            myRankingResponse = new VictoryFairyRackingResponse(
                    myRanking,
                    myRankingData.nickname(),
                    myRankingData.teamShortName(),
                    myRankingData.winPercent()
            );
        }

        return new VictoryFairyRackingResponses(topRankings, myRankingResponse);
    }
}
