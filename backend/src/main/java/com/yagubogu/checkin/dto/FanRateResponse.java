package com.yagubogu.checkin.dto;

import com.yagubogu.checkin.domain.FanRateGameEntry;
import java.util.ArrayList;
import java.util.List;

public record FanRateResponse(
        List<FanRateByGameResponse> fanRateByGames
) {

    public static FanRateResponse from(
            FanRateByGameResponse myTeamResponse,
            List<FanRateGameEntry> exceptMyTeam
    ) {
        List<FanRateByGameResponse> fanRateByGameResponses = new ArrayList<>();
        fanRateByGameResponses.add(myTeamResponse);
        exceptMyTeam.forEach(team -> fanRateByGameResponses.add(team.getResponse()));

        return new FanRateResponse(fanRateByGameResponses);
    }
}
