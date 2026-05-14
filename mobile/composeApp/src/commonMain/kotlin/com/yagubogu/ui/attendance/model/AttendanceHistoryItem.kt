package com.yagubogu.ui.attendance.model

import androidx.compose.ui.graphics.Color
import com.yagubogu.domain.model.GameResult
import com.yagubogu.domain.model.Team
import com.yagubogu.ui.theme.Gray400
import com.yagubogu.ui.util.color
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class AttendanceHistoryItem(
    val id: Long,
    val gameState: GameState,
    val dateTime: LocalDateTime,
    val stadiumName: String,
    val awayTeam: GameTeam,
    val homeTeam: GameTeam,
    val awayTeamScoreBoard: GameScoreBoard,
    val homeTeamScoreBoard: GameScoreBoard,
) {
    val awayTeamColor: Color
        get() = determineTeamColor(awayTeam)
    val homeTeamColor: Color
        get() = determineTeamColor(homeTeam)

    private fun determineTeamColor(team: GameTeam): Color =
        if (team.isMyTeam && team.gameResult == GameResult.WIN) {
            team.team.color
        } else {
            Gray400
        }

    @Serializable
    data class GameTeam(
        val team: Team,
        val name: String,
        val score: String,
        val isMyTeam: Boolean,
        val gameResult: GameResult,
        val type: TeamType,
    )

    @Serializable
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
}
