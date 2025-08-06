package com.yagubogu.presentation.favorite

import android.os.Parcelable
import com.yagubogu.domain.model.Team
import com.yagubogu.presentation.util.getEmoji
import kotlinx.parcelize.Parcelize

@Parcelize
data class FavoriteTeamUiModel(
    val id: Long,
    val name: String,
    val emoji: String,
) : Parcelable {
    companion object {
        fun of(team: Team): FavoriteTeamUiModel {
            val id: Long = team.ordinal.toLong()
            val name: String = team.nickName
            val emoji: String = team.getEmoji()
            return FavoriteTeamUiModel(id, name, emoji)
        }
    }
}
