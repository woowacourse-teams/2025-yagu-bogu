package com.yagubogu.stat.dto.v1;

import com.yagubogu.stat.dto.OpponentWinRateTeamParam;
import java.util.List;

public record OpponentWinRateResponse(
        List<OpponentWinRateTeamParam> opponents
) {
}
