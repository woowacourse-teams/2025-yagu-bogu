package com.yagubogu.game.dto;

import com.yagubogu.game.domain.Game;
import com.yagubogu.game.domain.ScoreBoard;
import java.util.List;

public record GameResultParam(
        ScoreBoardParam homeTeamScoreBoard,
        ScoreBoardParam awayTeamScoreBoard,
        String homePitcher,
        String awayPitcher
) {

    public static GameResultParam from(Game game) {
        return new GameResultParam(
                ScoreBoardParam.from(game.getHomeScoreBoard()),
                ScoreBoardParam.from(game.getAwayScoreBoard()),
                game.getHomePitcher(),
                game.getAwayPitcher()
        );
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
