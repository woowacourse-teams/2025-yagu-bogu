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
        // Sort stadiums by id to ensure deterministic order
        List<StadiumWithGameResponse> stadiums = gamesByStadium.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(Comparator.comparing(Stadium::getId)))
                .map(entry -> new StadiumWithGameResponse(
                        entry.getKey().getId(),
                        entry.getKey().getFullName(),
                        entry.getKey().getShortName(),
                        entry.getKey().getLocation(),
                        entry.getKey().getLatitude(),
                        entry.getKey().getLongitude(),
                        // Sort games by start time for stable comparison
                        GameResponse.from(entry.getValue().stream()
                                .sorted(Comparator.comparing(Game::getStartAt)
                                        .thenComparing(Game::getId))
                                .collect(Collectors.toList()))
                ))
                .toList();

        return new StadiumsWithGamesResponse(stadiums);
    }
}
