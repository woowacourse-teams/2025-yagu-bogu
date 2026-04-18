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
import org.jetbrains.compose.resources.DrawableResource
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.img_mascot_hh
import yagubogu.composeapp.generated.resources.img_mascot_ht
import yagubogu.composeapp.generated.resources.img_mascot_kt
import yagubogu.composeapp.generated.resources.img_mascot_lg
import yagubogu.composeapp.generated.resources.img_mascot_lt
import yagubogu.composeapp.generated.resources.img_mascot_nc
import yagubogu.composeapp.generated.resources.img_mascot_ob
import yagubogu.composeapp.generated.resources.img_mascot_sk
import yagubogu.composeapp.generated.resources.img_mascot_ss
import yagubogu.composeapp.generated.resources.img_mascot_wo

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

val Team.mascot: DrawableResource
    get() =
        when (this) {
            Team.HT -> Res.drawable.img_mascot_ht
            Team.LG -> Res.drawable.img_mascot_lg
            Team.WO -> Res.drawable.img_mascot_wo
            Team.KT -> Res.drawable.img_mascot_kt
            Team.SS -> Res.drawable.img_mascot_ss
            Team.LT -> Res.drawable.img_mascot_lt
            Team.SK -> Res.drawable.img_mascot_sk
            Team.NC -> Res.drawable.img_mascot_nc
            Team.HH -> Res.drawable.img_mascot_hh
            Team.OB -> Res.drawable.img_mascot_ob
        }
