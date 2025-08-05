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
