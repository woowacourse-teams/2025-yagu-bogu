package com.yagubogu.ui.home.model

data class VictoryFairyRanking(
    val topRankings: List<VictoryFairyItem> = emptyList(),
    val myRanking: VictoryFairyItem = VictoryFairyItem(),
)
