package com.yagubogu.game.dto;

import com.yagubogu.game.domain.ScoreBoard;

public record KboGameResultResponse(
        String statusCode,
        String msg,
        KboScoreBoardResponse homeScoreBoard,
        KboScoreBoardResponse awayScoreBoard
) {

    public record KboScoreBoardResponse(
            int runs,
            int hits,
            int errors,
            int basesOnBalls
    ) {

        public ScoreBoard toScoreBoard() {
            return new ScoreBoard(runs, hits, errors, basesOnBalls);
        }
    }
}
