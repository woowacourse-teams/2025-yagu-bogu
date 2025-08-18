package com.yagubogu.game.dto;

import com.yagubogu.game.domain.Game;
import com.yagubogu.game.domain.ScoreBoard;

public record GameResultResponse(
        ScoreBoardResponse homeTeamScoreBoard,
        ScoreBoardResponse awayTeamScoreBoard,
        String winningPitcher,
        String losingPitcher,
        String savePitcher,
        String holdPitcher
) {

    public static GameResultResponse from(Game game) {
        return new GameResultResponse(
                ScoreBoardResponse.from(game.getHomeScoreBoard()),
                ScoreBoardResponse.from(game.getAwayScoreBoard()),
                game.getPitchers().getWinningPitcher(),
                game.getPitchers().getLosingPitcher(),
                game.getPitchers().getSavePitcher(),
                game.getPitchers().getHoldPitcher()
        );
    }

    public record ScoreBoardResponse(
            Integer runs,
            Integer hits,
            Integer errors,
            Integer basesOnBalls,
            String inning1Score,
            String inning2Score,
            String inning3Score,
            String inning4Score,
            String inning5Score,
            String inning6Score,
            String inning7Score,
            String inning8Score,
            String inning9Score,
            String inning10Score,
            String inning11Score,
            String inning12Score
    ) {

        public static ScoreBoardResponse from(ScoreBoard sb) {
            if (sb == null) {
                return null;
            }
            return new ScoreBoardResponse(
                    sb.getRuns(),
                    sb.getHits(),
                    sb.getErrors(),
                    sb.getBasesOnBalls(),
                    safe(sb.getInning1Score()),
                    safe(sb.getInning2Score()),
                    safe(sb.getInning3Score()),
                    safe(sb.getInning4Score()),
                    safe(sb.getInning5Score()),
                    safe(sb.getInning6Score()),
                    safe(sb.getInning7Score()),
                    safe(sb.getInning8Score()),
                    safe(sb.getInning9Score()),
                    safe(sb.getInning10Score()),
                    safe(sb.getInning11Score()),
                    safe(sb.getInning12Score())
            );
        }

        private static String safe(Integer score) {
            return score == null ? "-" : String.valueOf(score);
        }
    }
}
