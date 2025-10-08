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
        val awayTeamColorRes: Int = determineTeamColorRes(awayTeam)

        @ColorRes
        val homeTeamColorRes: Int = determineTeamColorRes(homeTeam)

        @ColorRes
        private fun determineTeamColorRes(team: GameTeam): Int =
            if (team.isMyTeam && team.gameResult == GameResult.WIN) {
                team.teamColor
            } else {
                R.color.gray400
            }
    }

    data class Detail(
        val summary: Summary,
        val awayTeamPitcher: String,
        val homeTeamPitcher: String,
        val awayTeamScoreBoard: GameScoreBoard,
        val homeTeamScoreBoard: GameScoreBoard,
    ) : AttendanceHistoryItem(ViewType.DETAIL) {
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

    enum class ViewType {
        SUMMARY,
        DETAIL,
    }
}
