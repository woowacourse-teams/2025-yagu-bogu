package com.yagubogu.presentation.home.ranking

data class VictoryFairyRanking(
    val topRankings: List<VictoryFairyItem> = emptyList(),
    val myRanking: VictoryFairyItem = VictoryFairyItem(),
)
