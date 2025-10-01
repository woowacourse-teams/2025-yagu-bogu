package com.yagubogu.like.dto;

public record TeamLikeCountResponse(
        String teamCode,
        boolean isMyTeam,
        Long totalCount
) {
}
