package com.yagubogu.stat.dto;

import com.yagubogu.stadium.domain.Stadium;

public record LuckyStadiumResponse(
        String shortName
) {
    public static LuckyStadiumResponse from(final Stadium luckyStadium) {
        if (luckyStadium == null) {
            return new LuckyStadiumResponse(null);
        }
        return new LuckyStadiumResponse(luckyStadium.getShortName());
    }
}
