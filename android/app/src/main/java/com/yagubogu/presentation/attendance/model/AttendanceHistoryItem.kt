package com.yagubogu.presentation.attendance.model

import androidx.annotation.ColorRes
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
        val awayTeam: AttendanceHistoryTeamItem,
        val homeTeam: AttendanceHistoryTeamItem,
    ) : AttendanceHistoryItem(ViewType.SUMMARY) {
        @ColorRes
        val awayTeamColorRes: Int = determineTeamColorRes(awayTeam, homeTeam)

        @ColorRes
        val homeTeamColorRes: Int = determineTeamColorRes(homeTeam, awayTeam)

        @ColorRes
        private fun determineTeamColorRes(
            thisTeam: AttendanceHistoryTeamItem,
            otherTeam: AttendanceHistoryTeamItem,
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
    ) : AttendanceHistoryItem(ViewType.DETAIL)

    enum class ViewType {
        SUMMARY,
        DETAIL,
    }
}
