package com.yagubogu.presentation.util

import androidx.annotation.ColorRes
import com.yagubogu.R
import com.yagubogu.domain.model.Team

@ColorRes
fun Team.getTeamColor(): Int =
    when (this) {
        Team.KIA -> R.color.team_kia
        Team.LG -> R.color.team_lg
        Team.KIWOOM -> R.color.team_kiwoom
        Team.KT -> R.color.team_kt
        Team.SAMSUNG -> R.color.team_samsung
        Team.LOTTE -> R.color.team_lotte
        Team.SSG -> R.color.team_ssg
        Team.NC -> R.color.team_nc
        Team.HANWHA -> R.color.team_hanwha
        Team.DOOSAN -> R.color.team_doosan
    }

fun Team.getEmoji(): String =
    when (this) {
        Team.KIA -> "\uD83D\uDC2F"
        Team.LG -> "\uD83E\uDDD1\u200D\uD83E\uDD1D\u200D\uD83E\uDDD1"
        Team.KIWOOM -> "\uD83E\uDDB8"
        Team.KT -> "\uD83E\uDDD9"
        Team.SAMSUNG -> "\uD83E\uDD81"
        Team.LOTTE -> "\uD83C\uDF3A"
        Team.SSG -> "\uD83D\uDE80"
        Team.NC -> "\uD83E\uDD95"
        Team.HANWHA -> "\uD83E\uDD85"
        Team.DOOSAN -> "\uD83D\uDC3B"
    }
