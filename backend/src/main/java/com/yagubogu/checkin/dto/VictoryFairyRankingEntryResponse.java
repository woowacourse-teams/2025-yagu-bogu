package com.yagubogu.checkin.dto;

public record VictoryFairyRankingEntryResponse(
        Long memberId,
        String nickname,
        String teamShortName,
        Long totalCheckIns,
        double winPercent
) {
}
