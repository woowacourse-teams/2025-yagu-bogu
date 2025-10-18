package com.yagubogu.like.dto.v1;

import com.yagubogu.like.dto.TeamLikeCountParam;
import java.util.List;

public record LikeCountsResponse(
        long gameId,
        List<TeamLikeCountParam> counts
) {
}
