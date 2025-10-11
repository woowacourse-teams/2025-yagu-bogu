package com.yagubogu.presentation.home.ranking

data class VictoryFairyItem(
    val rank: Int,
    val nickname: String,
    val profileImageUrl: String,
    val teamName: String,
    val winRate: Double,
    // TODO API 배포 전 테스트
    val memberId: Long = 1,
)
