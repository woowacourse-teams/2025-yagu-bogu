package com.yagubogu.checkin.dto;

import com.yagubogu.member.domain.Member;
import java.util.ArrayList;
import java.util.List;

public record VictoryFairyRankingResponses(
        List<VictoryFairyRankingResponse> topRankings,
        VictoryFairyRankingResponse myRanking
) {

    public record VictoryFairyRankingResponse(
            int ranking,
            String nickname,
            String profileImageUrl,
            String teamShortName,
            double winPercent,
            double victoryFairyScore
    ) {

        public static VictoryFairyRankingResponse emptyRanking(
                Member myRankingData
        ) {
            return new VictoryFairyRankingResponse(
                    0,
                    myRankingData.getNickname().toString(),
                    myRankingData.getImageUrl(),
                    myRankingData.getTeam().getShortName(),
                    0,
                    0
            );
        }
    }

    public static VictoryFairyRankingResponses from(
            List<VictoryFairyRankingEntryResponse> topRankings,
            VictoryFairyRankingEntryResponse myRankingData,
            int myRanking
    ) {
        List<VictoryFairyRankingResponse> rankingResponses = getVictoryFairyRankingResponses(topRankings);

        VictoryFairyRankingResponse myRankingResponse = new VictoryFairyRankingResponse(
                myRanking,
                myRankingData.nickname(),
                myRankingData.profileImageUrl(),
                myRankingData.teamShortName(),
                myRankingData.winPercent(),
                myRankingData.victoryFairyScore()
        );

        return new VictoryFairyRankingResponses(rankingResponses, myRankingResponse);
    }

    private static List<VictoryFairyRankingResponse> getVictoryFairyRankingResponses(
            final List<VictoryFairyRankingEntryResponse> topRankings) {
        List<VictoryFairyRankingResponse> rankingResponses = new ArrayList<>();
        for (int i = 0; i < topRankings.size(); i++) {
            VictoryFairyRankingEntryResponse data = topRankings.get(i);
            rankingResponses.add(new VictoryFairyRankingResponse(
                    i + 1,
                    data.nickname(),
                    data.profileImageUrl(),
                    data.teamShortName(),
                    data.winPercent(),
                    data.victoryFairyScore()
            ));
        }
        return rankingResponses;
    }
}
