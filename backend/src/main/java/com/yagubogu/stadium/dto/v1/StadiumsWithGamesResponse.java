package com.yagubogu.stadium.dto.v1;

import com.yagubogu.game.domain.Game;
import com.yagubogu.stadium.domain.Stadium;
import com.yagubogu.stadium.dto.GameParam;
import com.yagubogu.stadium.dto.StadiumWithGameParam;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record StadiumsWithGamesResponse(
        List<StadiumWithGameParam> stadiums
) {

    public static StadiumsWithGamesResponse from(
            final Map<Stadium, List<Game>> gamesByStadium
    ) {
        List<StadiumWithGameParam> stadiums = gamesByStadium.entrySet().stream()
                .map(entry -> {
                    Stadium stadium = entry.getKey();
                    List<Game> games = entry.getValue();

                    return new StadiumWithGameParam(
                            stadium.getShortName(),
                            stadium.getLatitude(),
                            stadium.getLongitude(),
                            GameParam.from(games.stream()
                                    .sorted(Comparator.comparing(Game::getStartAt))
                                    .collect(Collectors.toList()))
                    );
                })
                .toList();

        return new StadiumsWithGamesResponse(stadiums);
    }
}
