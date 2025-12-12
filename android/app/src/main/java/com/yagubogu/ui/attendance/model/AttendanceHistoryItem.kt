package com.yagubogu.ui.attendance.model

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import com.yagubogu.R
import com.yagubogu.domain.model.GameResult
import com.yagubogu.ui.theme.Gray400
import com.yagubogu.ui.util.color
import java.time.LocalDate

sealed interface AttendanceHistoryItem {
    val summary: Summary

    data class Summary(
        val id: Long,
        val attendanceDate: LocalDate,
        val stadiumName: String,
        val awayTeam: GameTeam,
        val homeTeam: GameTeam,
    ) {
        val awayTeamColor: Color = determineTeamColor(awayTeam)
        val homeTeamColor: Color = determineTeamColor(homeTeam)

        private fun determineTeamColor(team: GameTeam): Color =
            if (team.isMyTeam && team.gameResult == GameResult.WIN) {
                team.team.color
            } else {
                Gray400
            }
    }

    data class Played(
        override val summary: Summary,
        val awayTeamPitcher: String,
        val homeTeamPitcher: String,
        val awayTeamScoreBoard: GameScoreBoard,
        val homeTeamScoreBoard: GameScoreBoard,
    ) : AttendanceHistoryItem {
        val awayTeam: GameTeam get() = summary.awayTeam
        val homeTeam: GameTeam get() = summary.homeTeam

        @StringRes
        val awayTeamPitcherStringRes: Int = determineTeamPitcher(awayTeam)

        @StringRes
        val homeTeamPitcherStringRes: Int = determineTeamPitcher(homeTeam)

        @StringRes
        private fun determineTeamPitcher(team: GameTeam): Int =
            when (team.gameResult) {
                GameResult.WIN -> R.string.attendance_history_winning_pitcher
                GameResult.DRAW -> R.string.attendance_history_draw_pitcher
                GameResult.LOSE -> R.string.attendance_history_losing_pitcher
            }
    }

    data class Canceled(
        override val summary: Summary,
    ) : AttendanceHistoryItem
}
