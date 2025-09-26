package com.yagubogu.data.dto.response.member

import com.yagubogu.ui.badge.model.BadgeUiModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDate

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
            description = "",
            isAcquired = true,
            achievedRate = 0,
            achievedAt = LocalDate.now(),
            progressRate = 0.0,
        )
}
