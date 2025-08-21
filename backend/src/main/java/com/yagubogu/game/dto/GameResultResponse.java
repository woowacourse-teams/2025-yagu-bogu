package com.yagubogu.game.dto;

import com.yagubogu.game.domain.Game;
import com.yagubogu.game.domain.ScoreBoard;
import java.util.List;

public record GameResultResponse(
        ScoreBoardResponse homeTeamScoreBoard,
        ScoreBoardResponse awayTeamScoreBoard,
        String homePitcher,
        String awayPitcher
) {

    public static GameResultResponse from(Game game) {
        return new GameResultResponse(
                ScoreBoardResponse.from(game.getHomeScoreBoard()),
                ScoreBoardResponse.from(game.getAwayScoreBoard()),
                game.getHomePitcher(),
                game.getAwayPitcher()
        );
    }

    public record ScoreBoardResponse(
            Integer runs,
            Integer hits,
            Integer errors,
            Integer basesOnBalls,
            List<String> inningScores
    ) {

        public static ScoreBoardResponse from(ScoreBoard sb) {
            if (sb == null) {
                return null;
            }
            List<String> inningScores = sb.getInningScores().stream()
                    .toList();

            return new ScoreBoardResponse(
                    sb.getRuns(),
                    sb.getHits(),
                    sb.getErrors(),
                    sb.getBasesOnBalls(),
                    inningScores
            );
        }
    }
}
