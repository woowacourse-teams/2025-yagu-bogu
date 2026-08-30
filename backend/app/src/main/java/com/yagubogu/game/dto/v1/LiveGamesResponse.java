package com.yagubogu.game.dto.v1;

import com.yagubogu.game.domain.Game;
import com.yagubogu.game.domain.GameState;
import com.yagubogu.game.domain.InningHalf;
import com.yagubogu.team.domain.Team;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

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
            if (game.getGameState() != GameState.LIVE) {
                return null;
            }
            return new LiveStateResponse(
                    requireLiveValue(game, game.getCurrentInning(), "inning"),
                    requireLiveValue(game, game.getCurrentInningHalf(), "inningHalf"),
                    BasesResponse.from(game),
                    BallCountResponse.from(game)
            );
        }
    }

    public record BasesResponse(
            boolean firstBaseOccupied,
            boolean secondBaseOccupied,
            boolean thirdBaseOccupied
    ) {

        private static BasesResponse from(final Game game) {
            return new BasesResponse(
                    requireLiveValue(game, game.getFirstBaseOccupied(), "firstBaseOccupied"),
                    requireLiveValue(game, game.getSecondBaseOccupied(), "secondBaseOccupied"),
                    requireLiveValue(game, game.getThirdBaseOccupied(), "thirdBaseOccupied")
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
                    requireLiveValue(game, game.getBalls(), "balls"),
                    requireLiveValue(game, game.getStrikes(), "strikes"),
                    requireLiveValue(game, game.getOuts(), "outs")
            );
        }
    }

    public enum CurrentPlayerRole {
        BATTER,
        PITCHER
    }

    private static <T> T requireLiveValue(final Game game, final T value, final String fieldName) {
        return Objects.requireNonNull(
                value,
                () -> "Live game has no " + fieldName + ": gameId=" + game.getId()
        );
    }
}
