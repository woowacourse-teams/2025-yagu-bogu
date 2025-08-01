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
    val awayTeamColorRes: Int = determineTeamColorRes(awayTeam)

    @ColorRes
    val homeTeamColorRes: Int = determineTeamColorRes(homeTeam)

    @ColorRes
    private fun determineTeamColorRes(team: TeamItem): Int {
        val myTeam: TeamItem = if (awayTeam.isMyTeam) awayTeam else homeTeam
        val opponentTeam: TeamItem = if (myTeam == awayTeam) homeTeam else awayTeam

        val gameResult = GameResult.from(myTeam.score, opponentTeam.score)
        return if (team.isMyTeam && gameResult == GameResult.WIN) {
            team.teamColor
        } else {
            R.color.gray400
        }
    }
}
