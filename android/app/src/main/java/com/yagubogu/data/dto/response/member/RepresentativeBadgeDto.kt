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
    @SerialName("type")
    val type: String,
) {
    fun toPresentation(): BadgeUiModel =
        BadgeUiModel(
            id = id,
            imageUrl = "TODO",
            name = name,
            description = "",
            isAcquired = true,
            achievedRate = 0,
            achievedAt = LocalDate.now(),
            progressRate = 0.0,
        )
}
