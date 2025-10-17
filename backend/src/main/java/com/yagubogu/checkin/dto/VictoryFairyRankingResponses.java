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
            long memberId,
            String nickname,
            String profileImageUrl,
            String teamShortName,
            double victoryFairyScore
    ) {

        public static VictoryFairyRankingResponse emptyRanking(
                Member member
        ) {
            return new VictoryFairyRankingResponse(
                    0,
                    member.getId(),
                    member.getNickname().toString(),
                    member.getImageUrl(),
                    member.getTeam().getShortName(),
                    0
            );
        }

        public static List<VictoryFairyRankingResponse> from(final List<VictoryFairyRank> victoryFairyRankings) {
            return victoryFairyRankings.stream()
                    .map(VictoryFairyRankingResponse::from)
                    .toList();
        }

        public static VictoryFairyRankingResponse from(final VictoryFairyRank victoryFairyRank) {
            return new VictoryFairyRankingResponse(
                    victoryFairyRank.rank(),
                    victoryFairyRank.memberId(),
                    victoryFairyRank.nickname(),
                    victoryFairyRank.profileImageUrl(),
                    victoryFairyRank.teamShortName(),
                    victoryFairyRank.score());
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
                myRankingData.memberId(),
                myRankingData.nickname(),
                myRankingData.profileImageUrl(),
                myRankingData.teamShortName(),
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
                    data.memberId(),
                    data.nickname(),
                    data.profileImageUrl(),
                    data.teamShortName(),
                    data.victoryFairyScore()
            ));
        }
        return rankingResponses;
    }
}
