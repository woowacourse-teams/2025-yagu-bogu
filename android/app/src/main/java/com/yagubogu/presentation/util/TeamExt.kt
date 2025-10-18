package com.yagubogu.presentation.util

import androidx.annotation.ColorRes
import com.yagubogu.R
import com.yagubogu.domain.model.Team

@ColorRes
fun Team.getTeamColor(): Int =
    when (this) {
        Team.HT -> R.color.team_kia
        Team.LG -> R.color.team_lg
        Team.WO -> R.color.team_kiwoom
        Team.KT -> R.color.team_kt
        Team.SS -> R.color.team_samsung
        Team.LT -> R.color.team_lotte
        Team.SK -> R.color.team_ssg
        Team.NC -> R.color.team_nc
        Team.HH -> R.color.team_hanwha
        Team.OB -> R.color.team_doosan
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

fun String.getTeam(): Team? = Team.entries.find { it.shortname == this }
