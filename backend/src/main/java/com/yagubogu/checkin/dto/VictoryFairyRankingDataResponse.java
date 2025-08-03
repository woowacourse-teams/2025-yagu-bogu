package com.yagubogu.checkin.dto;

public record VictoryFairyRankingDataResponse(
        Long memberId,
        String nickname,
        String teamShortName,
        Long totalCheckIns,
        double winPercent
) {
}
