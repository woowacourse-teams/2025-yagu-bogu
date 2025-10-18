package com.yagubogu.checkin.dto.v1;

import com.yagubogu.checkin.dto.VictoryFairyRankParam;
import com.yagubogu.checkin.dto.VictoryFairyRankingEntryParam;
import com.yagubogu.member.domain.Member;
import java.util.ArrayList;
import java.util.List;

public record VictoryFairyRankingResponse(
        List<VictoryFairyRankingParam> topRankings,
        VictoryFairyRankingParam myRanking
) {

    public record VictoryFairyRankingParam(
            int ranking,
            long memberId,
            String nickname,
            String profileImageUrl,
            String teamShortName,
            double victoryFairyScore
    ) {

        public static VictoryFairyRankingParam emptyRanking(
                Member member
        ) {
            return new VictoryFairyRankingParam(
                    0,
                    member.getId(),
                    member.getNickname().toString(),
                    member.getImageUrl(),
                    member.getTeam().getShortName(),
                    0
            );
        }

        public static List<VictoryFairyRankingParam> from(final List<VictoryFairyRankParam> victoryFairyRankings) {
            return victoryFairyRankings.stream()
                    .map(VictoryFairyRankingParam::from)
                    .toList();
        }

        public static VictoryFairyRankingParam from(final VictoryFairyRankParam victoryFairyRank) {
            return new VictoryFairyRankingParam(
                    victoryFairyRank.rank(),
                    victoryFairyRank.memberId(),
                    victoryFairyRank.nickname(),
                    victoryFairyRank.profileImageUrl(),
                    victoryFairyRank.teamShortName(),
                    victoryFairyRank.score());
        }
    }

    public static VictoryFairyRankingResponse from(
            List<VictoryFairyRankingEntryParam> topRankings,
            VictoryFairyRankingEntryParam myRankingData,
            int myRanking
    ) {
        List<VictoryFairyRankingParam> rankingResponses = getVictoryFairyRankingResponses(topRankings);

        VictoryFairyRankingParam myRankingResponse = new VictoryFairyRankingParam(
                myRanking,
                myRankingData.memberId(),
                myRankingData.nickname(),
                myRankingData.profileImageUrl(),
                myRankingData.teamShortName(),
                myRankingData.victoryFairyScore()
        );

        return new VictoryFairyRankingResponse(rankingResponses, myRankingResponse);
    }

    private static List<VictoryFairyRankingParam> getVictoryFairyRankingResponses(
            final List<VictoryFairyRankingEntryParam> topRankings) {
        List<VictoryFairyRankingParam> rankingResponses = new ArrayList<>();
        for (int i = 0; i < topRankings.size(); i++) {
            VictoryFairyRankingEntryParam data = topRankings.get(i);
            rankingResponses.add(new VictoryFairyRankingParam(
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
