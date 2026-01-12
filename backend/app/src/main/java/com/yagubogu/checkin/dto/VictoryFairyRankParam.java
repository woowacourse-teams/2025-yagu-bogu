package com.yagubogu.checkin.dto;

public record VictoryFairyRankParam(
        long rank,
        long memberId,
        double score,
        String nickname,
        String profileImageUrl,
        String teamShortName
) {
}
