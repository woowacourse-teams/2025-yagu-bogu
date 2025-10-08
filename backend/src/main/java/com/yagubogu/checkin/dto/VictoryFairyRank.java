package com.yagubogu.checkin.dto;

import com.yagubogu.stadium.domain.VictoryFairyRanking;
import java.util.List;

public record VictoryFairyRank(
        int rank,
        double score,
        String nickname,
        String profileImageUrl,
        String teamShortName
) {

    public static VictoryFairyRank from(VictoryFairyRanking victoryFairyRanking) {
        return new VictoryFairyRank(
                victoryFairyRanking.getScore(),
                victoryFairyRanking.getMember().getNickname().getValue(),
                victoryFairyRanking.getMember().getImageUrl(),
                victoryFairyRanking.getMember().getTeam().getShortName()
        );
    }

    public static List<VictoryFairyRank> from(List<VictoryFairyRanking> victoryFairyRankings) {
        return victoryFairyRankings
                .stream()
                .map(VictoryFairyRank::from)
                .toList();
    }
}
