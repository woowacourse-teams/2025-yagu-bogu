package com.yagubogu.checkin.dto;

public record VictoryFairyRankingEntryParam(
        Long memberId,
        String nickname,
        String profileImageUrl,
        String teamShortName,
        Long totalCheckIns,
        double winPercent,
        double victoryFairyScore
) {
}
