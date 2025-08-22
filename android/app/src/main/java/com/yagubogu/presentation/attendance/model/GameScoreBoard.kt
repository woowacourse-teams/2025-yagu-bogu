package com.yagubogu.presentation.attendance.model

data class GameScoreBoard(
    val runs: Int,
    val hits: Int,
    val errors: Int,
    val basesOnBalls: Int,
    private val scores: List<String>,
) {
    val inningScores: List<String> =
        if (scores.size >= NUMBER_OF_INNINGS) {
            scores.take(NUMBER_OF_INNINGS)
        } else {
            scores + List(NUMBER_OF_INNINGS - scores.size) { EMPTY_SCORE }
        }

    companion object {
        private const val NUMBER_OF_INNINGS = 11
        private const val EMPTY_SCORE = "-"
    }
}
