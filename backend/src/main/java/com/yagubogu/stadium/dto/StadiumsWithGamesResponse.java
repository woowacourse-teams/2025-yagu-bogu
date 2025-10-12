package com.yagubogu.stadium.dto;

import com.yagubogu.game.domain.Game;
import com.yagubogu.stadium.domain.Stadium;
import java.util.List;
import java.util.Map;

public record StadiumsWithGamesResponse(
        List<StadiumWithGameResponse> stadiums
) {
    public static StadiumsWithGamesResponse from(final Map<Stadium, List<Game>> gamesByStadium) {
        List<StadiumWithGameResponse> stadiums = gamesByStadium.entrySet().stream()
                .map(entry -> new StadiumWithGameResponse(
                        entry.getKey().getId(),
                        entry.getKey().getFullName(),
                        entry.getKey().getShortName(),
                        entry.getKey().getLocation(),
                        entry.getKey().getLatitude(),
                        entry.getKey().getLongitude(),
                        GameResponse.from(entry.getValue())
                )).toList();

        return new StadiumsWithGamesResponse(stadiums);
    }
}
