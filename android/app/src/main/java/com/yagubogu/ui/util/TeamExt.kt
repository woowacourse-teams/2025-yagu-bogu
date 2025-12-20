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
