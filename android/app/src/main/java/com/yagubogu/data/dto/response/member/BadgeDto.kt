package com.yagubogu.data.dto.response.member

import com.yagubogu.ui.badge.model.BadgeUiModel
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toJavaLocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BadgeDto(
    @SerialName("id")
    val id: Long,
    @SerialName("name")
    val name: String,
    @SerialName("description")
    val description: String,
    @SerialName("policy")
    val policy: String,
    @SerialName("acquired")
    val acquired: Boolean,
    @SerialName("achievedAt")
    val achievedAt: LocalDateTime?,
    @SerialName("badgeImageUrl")
    val badgeImageUrl: String,
    @SerialName("progressRate")
    val progressRate: Double,
    @SerialName("achievedRate")
    val achievedRate: Double,
) {
    fun toPresentation(): BadgeUiModel =
        BadgeUiModel(
            id = id,
            imageUrl = badgeImageUrl,
            name = name,
            description = description,
            isAcquired = acquired,
            achievedRate = achievedRate.toInt(),
            achievedAt = achievedAt?.date?.toJavaLocalDate(),
            progressRate = progressRate,
        )
}
