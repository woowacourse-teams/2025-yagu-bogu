package com.yagubogu.like.dto;

import java.util.List;

public record LikeCountsResponse(
        long gameId,
        List<TeamLikeCountResponse> counts
) {
}
