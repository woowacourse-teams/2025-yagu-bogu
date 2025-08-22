package com.yagubogu.stat.dto;

import java.util.List;

public record OpponentWinRateResponse(
        List<OpponentWinRateTeamResponse> opponents
) {
}
