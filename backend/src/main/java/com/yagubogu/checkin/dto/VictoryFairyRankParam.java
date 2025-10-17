package com.yagubogu.checkin.dto;

public record VictoryFairyRankParam(
        double score,
        String nickname,
        String profileImageUrl,
        String teamShortName,
        double winPercent
) {
}
