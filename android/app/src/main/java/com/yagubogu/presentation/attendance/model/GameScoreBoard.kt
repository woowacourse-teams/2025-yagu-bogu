package com.yagubogu.presentation.attendance.model

data class GameScoreBoard(
    val runs: Int,
    val hits: Int,
    val errors: Int,
    val basesOnBalls: Int,
    private val scores: List<String>,
) {
    val inningScore: List<String> =
        if (scores.size >= NUMBER_OF_INNINGS) {
            scores.take(NUMBER_OF_INNINGS)
        } else {
            scores + List(NUMBER_OF_INNINGS - scores.size) { EMPTY_SCORE }
        }

    companion object {
        private const val NUMBER_OF_INNINGS = 11
        private const val EMPTY_SCORE = "-"

        val DUMMY_DATA =
            GameScoreBoard(
                runs = 9,
                hits = 13,
                errors = 1,
                basesOnBalls = 5,
                scores = listOf("1", "0", "0", "4", "0", "0", "2", "2", "0"),
            )
    }
}
