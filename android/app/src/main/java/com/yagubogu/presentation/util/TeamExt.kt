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
