package com.yagubogu.game.dto;

import com.yagubogu.game.domain.ScoreBoard;

public record KboGameResultResponse(
        ScoreBoard homeScoreBoard,
        ScoreBoard awayScoreBoard,
        String homePitcher,
        String awayPitcher
) {
}
