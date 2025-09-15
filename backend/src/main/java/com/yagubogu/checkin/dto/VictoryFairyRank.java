package com.yagubogu.checkin.dto;

public record VictoryFairyRank(
        double score,
        String nickname,
        String profileImageUrl,
        String teamShortName,
        double winPercent
) {
}
