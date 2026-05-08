package com.yagubogu.ui.ranking.model

sealed interface RankingProfileItem {
    val memberId: Long
    val rank: Long
    val nickname: String
    val profileImageUrl: String
    val teamName: String

    data class CheckInRanking(
        override val memberId: Long = 0L,
        override val rank: Long = 0,
        override val nickname: String = "",
        override val profileImageUrl: String = "",
        override val teamName: String = "",
        val count: Int = 0,
    ) : RankingProfileItem

    data class VictoryFairyRanking(
        override val memberId: Long = 0L,
        override val rank: Long = 0,
        override val nickname: String = "",
        override val profileImageUrl: String = "",
        override val teamName: String = "",
        val score: Double = 0.0,
    ) : RankingProfileItem
}
