package com.yagubogu.checkin.dto;

import com.yagubogu.checkin.domain.FanRateGameEntry;
import java.util.ArrayList;
import java.util.List;

public record FanRateResponse(
        List<FanRateByGameResponse> fanRateByGames
) {

    public static FanRateResponse from(
            FanRateByGameResponse myTeamEnterThisGame,
            List<FanRateGameEntry> pairs
    ) {

        List<FanRateByGameResponse> fanRateByGameResponses = new ArrayList<>();
        fanRateByGameResponses.add(myTeamEnterThisGame);
        pairs.forEach(pair -> fanRateByGameResponses.add(pair.getResponse()));

        return new FanRateResponse(fanRateByGameResponses);
    }
}
