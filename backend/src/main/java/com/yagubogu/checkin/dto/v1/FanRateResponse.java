package com.yagubogu.checkin.dto.v1;

import com.yagubogu.checkin.dto.FanRateByGameParam;
import com.yagubogu.checkin.dto.FanRateGameParam;
import java.util.ArrayList;
import java.util.List;

public record FanRateResponse(
        List<FanRateByGameParam> fanRateByGames
) {

    public static FanRateResponse from(
            FanRateByGameParam myTeamResponse,
            List<FanRateGameParam> exceptMyTeam
    ) {
        List<FanRateByGameParam> fanRateByGameResponse = new ArrayList<>();
        if (myTeamResponse != null) {
            fanRateByGameResponse.add(myTeamResponse);
        }

        List<FanRateByGameParam> sortedOthers = exceptMyTeam.stream()
                .map(FanRateGameParam::getResponse)
                .sorted()
                .toList();
        fanRateByGameResponse.addAll(sortedOthers);

        return new FanRateResponse(fanRateByGameResponse);
    }
}
