package com.yagubogu.presentation.favorite

import android.os.Parcelable
import com.yagubogu.domain.model.Team
import com.yagubogu.ui.util.getEmoji
import kotlinx.parcelize.Parcelize

@Parcelize
data class FavoriteTeamItem(
    val team: Team,
    val emoji: String,
) : Parcelable {
    companion object {
        fun of(team: Team): FavoriteTeamItem {
            val emoji: String = team.getEmoji()
            return FavoriteTeamItem(team, emoji)
        }
    }
}
