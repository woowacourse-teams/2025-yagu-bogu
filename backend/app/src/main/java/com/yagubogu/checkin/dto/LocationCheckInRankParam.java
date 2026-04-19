package com.yagubogu.checkin.dto;

public record LocationCheckInRankParam(
        long memberId,
        String nickname,
        String profileImageUrl,
        String teamShortName,
        long visitCount
) {
}
