package com.yagubogu.domain.model

enum class GameResult {
    WIN,
    DRAW,
    LOSE,
    ;

    companion object {
        fun from(
            myScore: Int,
            opponentScore: Int,
        ): GameResult =
            when {
                myScore > opponentScore -> WIN
                myScore < opponentScore -> LOSE
                else -> DRAW
            }
    }
}
