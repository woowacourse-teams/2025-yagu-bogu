package com.yagubogu.presentation.home.model

import androidx.annotation.ColorRes
import com.yagubogu.R
import com.yagubogu.domain.model.Team
import com.yagubogu.presentation.util.getTeamColor
import kotlin.math.roundToInt

data class TeamOccupancyStatus(
    val team: Team?,
    val percentage: Double,
) {
    val roundedPercentage: Int
        get() = percentage.roundToInt()

    @ColorRes
    val teamColor: Int = team?.getTeamColor() ?: R.color.gray300

    val teamName: String = team?.shortName ?: TEAM_NAME_ETC

    companion object {
        private const val TEAM_NAME_ETC = "기타"
    }
}
