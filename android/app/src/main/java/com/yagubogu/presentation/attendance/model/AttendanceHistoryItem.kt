package com.yagubogu.presentation.attendance.model

import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import com.yagubogu.R
import com.yagubogu.domain.model.GameResult
import java.time.LocalDate

sealed class AttendanceHistoryItem(
    val type: ViewType,
) {
    data class Summary(
        val id: Long,
        val attendanceDate: LocalDate,
        val stadiumName: String,
        val awayTeam: GameTeam,
        val homeTeam: GameTeam,
    ) : AttendanceHistoryItem(ViewType.SUMMARY) {
        @ColorRes
        val awayTeamColorRes: Int = determineTeamColorRes(awayTeam, homeTeam)

        @ColorRes
        val homeTeamColorRes: Int = determineTeamColorRes(homeTeam, awayTeam)

        @ColorRes
        private fun determineTeamColorRes(
            thisTeam: GameTeam,
            otherTeam: GameTeam,
        ): Int {
            val gameResult = GameResult.from(thisTeam.score, otherTeam.score)
            return if (thisTeam.isMyTeam && gameResult == GameResult.WIN) {
                thisTeam.teamColor
            } else {
                R.color.gray400
            }
        }
    }

    data class Detail(
        val summary: Summary,
        val awayTeamScoreBoard: GameScoreBoard,
        val homeTeamScoreBoard: GameScoreBoard,
    ) : AttendanceHistoryItem(ViewType.DETAIL) {
        @StringRes
        val awayTeamPitcherRes: Int = determineTeamPitcher(summary.awayTeam, summary.homeTeam)
        val awayTeamPitcherName: String = summary.awayTeam.pitcher

        @StringRes
        val homeTeamPitcherRes: Int = determineTeamPitcher(summary.homeTeam, summary.awayTeam)
        val homeTeamPitcherName: String = summary.homeTeam.pitcher

        @StringRes
        private fun determineTeamPitcher(
            thisTeam: GameTeam,
            otherTeam: GameTeam,
        ): Int {
            val gameResult = GameResult.from(thisTeam.score, otherTeam.score)
            return when (gameResult) {
                GameResult.WIN -> R.string.attendance_history_winning_pitcher
                GameResult.DRAW -> R.string.attendance_history_draw_pitcher
                GameResult.LOSE -> R.string.attendance_history_losing_pitcher
            }
        }
    }

    enum class ViewType {
        SUMMARY,
        DETAIL,
    }
}
