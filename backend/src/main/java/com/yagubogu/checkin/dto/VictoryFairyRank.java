package com.yagubogu.checkin.dto;

public record VictoryFairyRank(
        int rank,
        long memberId,
        double score,
        String nickname,
        String profileImageUrl,
        String teamShortName
) {
}
