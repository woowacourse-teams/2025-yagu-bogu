package com.yagubogu.presentation.attendance.model

data class GameScoreBoard(
    val runs: Int,
    val hits: Int,
    val errors: Int,
    val basesOnBalls: Int,
    val inningScore: List<String>,
) {
    companion object {
        val DUMMY_DATA =
            GameScoreBoard(
                runs = 9,
                hits = 13,
                errors = 1,
                basesOnBalls = 5,
                inningScore = listOf("1", "0", "0", "4", "0", "0", "2", "2", "0", "-", "-"),
            )
    }
}
