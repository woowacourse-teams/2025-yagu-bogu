package com.yagubogu.presentation.livetalk.stadium

import androidx.annotation.ColorRes
import com.yagubogu.domain.model.Team
import com.yagubogu.presentation.util.getEmoji
import com.yagubogu.presentation.util.getTeamColor

data class LivetalkStadiumItem(
    val stadiumName: String,
    val userCount: Int,
    val awayTeam: Team,
    val homeTeam: Team,
    val isVerified: Boolean,
) {
    @ColorRes
    val awayTeamColor: Int = awayTeam.getTeamColor()
    val awayTeamEmoji: String = awayTeam.getEmoji()

    @ColorRes
    val homeTeamColor: Int = homeTeam.getTeamColor()
    val homeTeamEmoji: String = homeTeam.getEmoji()
}
