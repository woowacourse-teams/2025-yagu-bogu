package com.yagubogu.game.dto.v1;

import com.yagubogu.game.domain.Game;
import com.yagubogu.game.domain.GameState;
import com.yagubogu.game.domain.InningHalf;
import com.yagubogu.team.domain.Team;
import java.time.LocalTime;
import java.util.List;

public record LiveGamesResponse(
        List<LiveGameResponse> games
) {

    public static LiveGamesResponse from(final List<Game> games) {
        return new LiveGamesResponse(games.stream()
                .map(LiveGameResponse::from)
                .toList());
    }

    public record LiveGameResponse(
            long gameId,
            LiveTeamResponse homeTeam,
            LiveTeamResponse awayTeam,
            GameState gameState,
            LocalTime startAt,
            LiveStateResponse liveState
    ) {

        private static LiveGameResponse from(final Game game) {
            return new LiveGameResponse(
                    game.getId(),
                    LiveTeamResponse.homeFrom(game),
                    LiveTeamResponse.awayFrom(game),
                    game.getGameState(),
                    game.getStartAt(),
                    LiveStateResponse.from(game)
            );
        }
    }

    public record LiveTeamResponse(
            String code,
            String name,
            String currentPlayer,
            Integer score
    ) {

        private static LiveTeamResponse homeFrom(final Game game) {
            return from(game.getHomeTeam(), currentPlayer(game, "home"), game.getHomeScore());
        }

        private static LiveTeamResponse awayFrom(final Game game) {
            return from(game.getAwayTeam(), currentPlayer(game, "away"), game.getAwayScore());
        }

        private static LiveTeamResponse from(final Team team, final String currentPlayer, final Integer score) {
            return new LiveTeamResponse(team.getTeamCode(), team.getName(), currentPlayer, score);
        }

        private static String currentPlayer(final Game game, final String teamSide) {
            if (game.getGameState() != GameState.LIVE) {
                return null;
            }
            if (teamSide.equals(game.getCurrentBatterTeam())) {
                return game.getCurrentBatterName();
            }
            if (teamSide.equals(game.getCurrentPitcherTeam())) {
                return game.getCurrentPitcherName();
            }
            return null;
        }
    }

    public record LiveStateResponse(
            Integer inning,
            InningHalf inningHalf,
            BasesResponse bases,
            BallCountResponse count
    ) {

        private static LiveStateResponse from(final Game game) {
            if (game.getGameState() != GameState.LIVE) {
                return null;
            }
            return new LiveStateResponse(
                    game.getCurrentInning(),
                    game.getCurrentInningHalf(),
                    BasesResponse.from(game),
                    BallCountResponse.from(game)
            );
        }
    }

    public record BasesResponse(
            Boolean firstBaseOccupied,
            Boolean secondBaseOccupied,
            Boolean thirdBaseOccupied
    ) {

        private static BasesResponse from(final Game game) {
            return new BasesResponse(
                    game.getFirstBaseOccupied(),
                    game.getSecondBaseOccupied(),
                    game.getThirdBaseOccupied()
            );
        }
    }

    public record BallCountResponse(
            Integer balls,
            Integer strikes,
            Integer outs
    ) {

        private static BallCountResponse from(final Game game) {
            return new BallCountResponse(game.getBalls(), game.getStrikes(), game.getOuts());
        }
    }
}
