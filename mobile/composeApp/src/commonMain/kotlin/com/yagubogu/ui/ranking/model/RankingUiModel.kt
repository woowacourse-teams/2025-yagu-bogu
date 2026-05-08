package com.yagubogu.ui.ranking.model

import androidx.compose.runtime.Immutable
import com.yagubogu.ui.common.model.MemberProfile
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class RankingUiModel(
    val type: RankingType,
    val topRankings: ImmutableList<RankingProfileItem> = persistentListOf(),
    val myRanking: RankingProfileItem =
        when (type) {
            RankingType.CHECK_IN -> RankingProfileItem.CheckInRanking()
            RankingType.VICTORY_FAIRY -> RankingProfileItem.VictoryFairyRanking()
        },
    val nextCursorId: Long? = null,
    val hasNext: Boolean = true,
    val isLoading: Boolean = false,
    val selectedMemberProfile: MemberProfile? = null,
)
