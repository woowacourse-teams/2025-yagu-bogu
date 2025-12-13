package com.yagubogu.ui.util

import androidx.compose.ui.graphics.Color
import com.yagubogu.domain.model.Team
import com.yagubogu.ui.theme.TeamDoosan
import com.yagubogu.ui.theme.TeamHanwha
import com.yagubogu.ui.theme.TeamKia
import com.yagubogu.ui.theme.TeamKiwoom
import com.yagubogu.ui.theme.TeamKt
import com.yagubogu.ui.theme.TeamLg
import com.yagubogu.ui.theme.TeamLotte
import com.yagubogu.ui.theme.TeamNc
import com.yagubogu.ui.theme.TeamSamsung
import com.yagubogu.ui.theme.TeamSsg

val Team.color: Color
    get() =
        when (this) {
            Team.HT -> TeamKia
            Team.LG -> TeamLg
            Team.WO -> TeamKiwoom
            Team.KT -> TeamKt
            Team.SS -> TeamSamsung
            Team.LT -> TeamLotte
            Team.SK -> TeamSsg
            Team.NC -> TeamNc
            Team.HH -> TeamHanwha
            Team.OB -> TeamDoosan
        }

fun Team.getEmoji(): String =
    when (this) {
        Team.HT -> "\uD83D\uDC2F"
        Team.LG -> "\uD83E\uDDD1\u200D\uD83E\uDD1D\u200D\uD83E\uDDD1"
        Team.WO -> "\uD83E\uDDB8"
        Team.KT -> "\uD83E\uDDD9"
        Team.SS -> "\uD83E\uDD81"
        Team.LT -> "\uD83C\uDF3A"
        Team.SK -> "\uD83D\uDE80"
        Team.NC -> "\uD83E\uDD95"
        Team.HH -> "\uD83E\uDD85"
        Team.OB -> "\uD83D\uDC3B"
    }
