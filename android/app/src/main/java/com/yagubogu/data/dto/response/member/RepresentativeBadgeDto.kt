package com.yagubogu.data.dto.response.member

import com.yagubogu.ui.badge.model.BadgeUiModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RepresentativeBadgeDto(
    @SerialName("id")
    val id: Long,
    @SerialName("name")
    val name: String,
    @SerialName("policy")
    val policy: String,
    @SerialName("badgeImageUrl")
    val badgeImageUrl: String,
) {
    fun toPresentation(): BadgeUiModel =
        BadgeUiModel(
            id = id,
            imageUrl = badgeImageUrl,
            name = name,
            isAcquired = true,
        )
}
