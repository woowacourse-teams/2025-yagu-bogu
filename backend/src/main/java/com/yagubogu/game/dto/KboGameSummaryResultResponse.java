package com.yagubogu.game.dto;

import com.yagubogu.game.domain.ScoreBoardSummary;

public record KboGameSummaryResultResponse(
        String statusCode,
        String msg,
        KboScoreBoardSummaryResponse homeScoreBoard,
        KboScoreBoardSummaryResponse awayScoreBoard
) {

    public record KboScoreBoardSummaryResponse(
            int runs,
            int hits,
            int errors,
            int basesOnBalls
    ) {

        public ScoreBoardSummary toScoreBoard() {
            return new ScoreBoardSummary(runs, hits, errors, basesOnBalls);
        }
    }
}
