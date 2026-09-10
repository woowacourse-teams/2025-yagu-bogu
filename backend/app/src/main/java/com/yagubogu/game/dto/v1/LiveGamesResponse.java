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
            CurrentPlayerRole currentPlayerRole,
            Integer score
    ) {

        private static LiveTeamResponse homeFrom(final Game game) {
            return from(
                    game.getHomeTeam(),
                    currentPlayer(game, "home"),
                    currentPlayerRole(game, "home"),
                    game.getHomeScore()
            );
        }

        private static LiveTeamResponse awayFrom(final Game game) {
            return from(
                    game.getAwayTeam(),
                    currentPlayer(game, "away"),
                    currentPlayerRole(game, "away"),
                    game.getAwayScore()
            );
        }

        private static LiveTeamResponse from(
                final Team team,
                final String currentPlayer,
                final CurrentPlayerRole currentPlayerRole,
                final Integer score
        ) {
            return new LiveTeamResponse(
                    team.getTeamCode(),
                    team.getName(),
                    currentPlayer,
                    currentPlayerRole,
                    score
            );
        }

        private static String currentPlayer(final Game game, final String teamSide) {
            if (game.getGameState() == GameState.SCHEDULED) {
                return probablePitcher(game, teamSide);
            }
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

        private static CurrentPlayerRole currentPlayerRole(final Game game, final String teamSide) {
            if (game.getGameState() == GameState.SCHEDULED && probablePitcher(game, teamSide) != null) {
                return CurrentPlayerRole.PITCHER;
            }
            if (game.getGameState() != GameState.LIVE) {
                return null;
            }
            if (teamSide.equals(game.getCurrentBatterTeam()) && game.getCurrentBatterName() != null) {
                return CurrentPlayerRole.BATTER;
            }
            if (teamSide.equals(game.getCurrentPitcherTeam()) && game.getCurrentPitcherName() != null) {
                return CurrentPlayerRole.PITCHER;
            }
            return null;
        }

        private static String probablePitcher(final Game game, final String teamSide) {
            if ("home".equals(teamSide)) {
                return game.getHomeProbablePitcher();
            }
            return game.getAwayProbablePitcher();
        }
    }

    public record LiveStateResponse(
            int inning,
            InningHalf inningHalf,
            BasesResponse bases,
            BallCountResponse count
    ) {

        private static LiveStateResponse from(final Game game) {
            if (game.getGameState() != GameState.LIVE || hasIncompleteLiveState(game)) {
                return null;
            }
            return new LiveStateResponse(
                    game.getCurrentInning(),
                    game.getCurrentInningHalf(),
                    BasesResponse.from(game),
                    BallCountResponse.from(game)
            );
        }

        private static boolean hasIncompleteLiveState(final Game game) {
            return game.getCurrentInning() == null
                    || game.getCurrentInningHalf() == null
                    || game.getFirstBaseOccupied() == null
                    || game.getSecondBaseOccupied() == null
                    || game.getThirdBaseOccupied() == null
                    || game.getBalls() == null
                    || game.getStrikes() == null
                    || game.getOuts() == null;
        }
    }

    public record BasesResponse(
            boolean firstBaseOccupied,
            boolean secondBaseOccupied,
            boolean thirdBaseOccupied
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
            int balls,
            int strikes,
            int outs
    ) {

        private static BallCountResponse from(final Game game) {
            return new BallCountResponse(
                    game.getBalls(),
                    game.getStrikes(),
                    game.getOuts()
            );
        }
    }

    public enum CurrentPlayerRole {
        BATTER,
        PITCHER
    }

}
