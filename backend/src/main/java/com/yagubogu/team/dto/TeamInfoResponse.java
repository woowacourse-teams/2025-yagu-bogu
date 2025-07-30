package com.yagubogu.team.dto;

public record TeamInfoResponse(
        Long id,
        String name,
        int score,
        boolean isMyTeam
) {
}
