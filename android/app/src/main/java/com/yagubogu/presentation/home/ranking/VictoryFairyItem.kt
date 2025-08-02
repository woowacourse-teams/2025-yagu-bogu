package com.yagubogu.presentation.home.ranking

import com.yagubogu.domain.model.Team

data class VictoryFairyItem(
    val rank: Int,
    val profileImageUrl: String,
    val nickname: String,
    val team: Team,
    val winRate: Double,
)
