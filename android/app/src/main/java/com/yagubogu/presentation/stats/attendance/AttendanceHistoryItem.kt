package com.yagubogu.presentation.stats.attendance

import androidx.annotation.ColorRes
import com.yagubogu.R
import com.yagubogu.domain.model.GameResult
import java.time.LocalDate

data class AttendanceHistoryItem(
    val awayTeam: TeamItem,
    val homeTeam: TeamItem,
    val attendanceDate: LocalDate,
    val stadiumName: String,
) {
    @ColorRes
    val awayTeamColorRes: Int = determineTeamColorRes(awayTeam, homeTeam)

    @ColorRes
    val homeTeamColorRes: Int = determineTeamColorRes(homeTeam, awayTeam)

    @ColorRes
    private fun determineTeamColorRes(
        thisTeam: TeamItem,
        otherTeam: TeamItem,
    ): Int {
        val gameResult = GameResult.from(thisTeam.score, otherTeam.score)
        return if (thisTeam.isMyTeam && gameResult == GameResult.WIN) {
            thisTeam.teamColor
        } else {
            R.color.gray400
        }
    }
}
