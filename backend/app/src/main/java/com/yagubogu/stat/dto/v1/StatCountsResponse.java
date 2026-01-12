package com.yagubogu.stat.dto.v1;

public record StatCountsResponse(
        int winCounts,
        int drawCounts,
        int loseCounts,
        int favoriteCheckInCounts
) {
}
