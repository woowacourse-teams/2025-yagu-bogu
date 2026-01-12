package com.yagubogu.game.dto;

import com.yagubogu.game.domain.ScoreBoard;

public record KboGameResultParam(
        ScoreBoard homeScoreBoard,
        ScoreBoard awayScoreBoard,
        String homePitcher,
        String awayPitcher
) {
}
