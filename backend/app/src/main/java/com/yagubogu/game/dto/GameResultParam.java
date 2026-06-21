package com.yagubogu.game.dto;

import com.yagubogu.game.domain.Game;
import com.yagubogu.game.domain.ScoreBoard;
import java.util.List;

public record GameResultParam(
        ScoreBoardParam homeTeamScoreBoard,
        ScoreBoardParam awayTeamScoreBoard,
        String homePitcher,
        String awayPitcher,
        LiveStateParam liveState
) {

    public static GameResultParam from(Game game) {
        return new GameResultParam(
                ScoreBoardParam.from(game.getHomeScoreBoard()),
                ScoreBoardParam.from(game.getAwayScoreBoard()),
                game.getHomePitcher(),
                game.getAwayPitcher(),
                LiveStateParam.from(game)
        );
    }

    public record LiveStateParam(
            String currentBatterTeam,
            String currentBatterName,
            String currentPitcherTeam,
            String currentPitcherName,
            Boolean firstBaseOccupied,
            Boolean secondBaseOccupied,
            Boolean thirdBaseOccupied,
            Integer balls,
            Integer strikes,
            Integer outs
    ) {

        public static LiveStateParam from(Game game) {
            return new LiveStateParam(
                    game.getCurrentBatterTeam(),
                    game.getCurrentBatterName(),
                    game.getCurrentPitcherTeam(),
                    game.getCurrentPitcherName(),
                    game.getFirstBaseOccupied(),
                    game.getSecondBaseOccupied(),
                    game.getThirdBaseOccupied(),
                    game.getBalls(),
                    game.getStrikes(),
                    game.getOuts()
            );
        }
    }

    public record ScoreBoardParam(
            Integer runs,
            Integer hits,
            Integer errors,
            Integer basesOnBalls,
            List<String> inningScores
    ) {

        public static ScoreBoardParam from(ScoreBoard sb) {
            if (sb == null) {
                return null;
            }
            List<String> inningScores = sb.getInningScores().stream()
                    .toList();

            return new ScoreBoardParam(
                    sb.getRuns(),
                    sb.getHits(),
                    sb.getErrors(),
                    sb.getBasesOnBalls(),
                    inningScores
            );
        }
    }
}
