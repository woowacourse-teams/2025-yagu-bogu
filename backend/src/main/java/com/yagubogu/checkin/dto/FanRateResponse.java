package com.yagubogu.checkin.dto;

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

        List<FanRateByGameResponse> sortedOthers = exceptMyTeam.stream()
                .map(FanRateGameEntry::getResponse)
                .sorted()
                .toList();

        fanRateByGameResponses.addAll(sortedOthers);

        return new FanRateResponse(fanRateByGameResponses);
    }
}
