package com.yagubogu.ui.home.component

import com.yagubogu.presentation.home.ranking.VictoryFairyItem
import com.yagubogu.presentation.home.ranking.VictoryFairyRanking

val VICTORY_FAIRY_RANKING_ITEM =
    VictoryFairyItem(
        rank = 1,
        nickname = "닉네임",
        teamName = "KIA",
        score = 100.0,
    )

val VICTORY_FAIRY_RANKING =
    VictoryFairyRanking(
        topRankings =
            List(5) { index: Int ->
                VICTORY_FAIRY_RANKING_ITEM.copy(rank = index + 1)
            },
        myRanking = VICTORY_FAIRY_RANKING_ITEM,
    )
