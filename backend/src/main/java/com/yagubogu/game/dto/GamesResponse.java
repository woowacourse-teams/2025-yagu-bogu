package com.yagubogu.game.dto;

import com.yagubogu.game.domain.Game;
import java.util.List;

public record GamesResponse(List<GameResponse> games) {

    public static GamesResponse from(List<Game> games) {
        return new GamesResponse(
                games.stream()
                        .map(GameResponse::from)
                        .toList()
        );
    }

    public record GameResponse(
            StadiumInfoResponse stadium,
            TeamInfoResponse homeTeam,
            TeamInfoResponse awayTeam
    ) {

        public static GameResponse from(Game game) {
            return new GameResponse(
                    new StadiumInfoResponse(
                            game.getStadium().getId(),
                            game.getStadium().getFullName()
                    ),
                    new TeamInfoResponse(
                            game.getHomeTeam().getId(),
                            game.getHomeTeam().getName(),
                            game.getHomeTeam().getTeamCode()
                    ),
                    new TeamInfoResponse(
                            game.getAwayTeam().getId(),
                            game.getAwayTeam().getName(),
                            game.getAwayTeam().getTeamCode()
                    )
            );
        }
    }

    public record StadiumInfoResponse(Long id, String name) {
    }

    public record TeamInfoResponse(Long id, String name, String code) {
    }
}
