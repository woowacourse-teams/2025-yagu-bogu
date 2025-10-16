package com.yagubogu.stadium.dto;

import com.yagubogu.game.domain.Game;
import com.yagubogu.stadium.domain.Stadium;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
                        GameResponse.from(entry.getValue().stream()
                                .sorted(Comparator.comparing(Game::getStartAt))
                                .collect(Collectors.toList()))
                ))
                .toList();

        return new StadiumsWithGamesResponse(stadiums);
    }
}
