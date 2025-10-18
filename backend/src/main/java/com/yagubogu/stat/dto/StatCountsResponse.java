package com.yagubogu.stat.dto;

public record StatCountsResponse(
        int winCounts,
        int drawCounts,
        int loseCounts,
        int favoriteCheckInCounts
) {
}
