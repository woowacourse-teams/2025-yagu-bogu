package com.yagubogu.like.dto;

import java.util.Map;

public record LikeCountsResponse(
        long gameId,
        Map<Long, Long> counts
) {
}
