package com.yagubogu.data.dto.response.member

import com.yagubogu.ui.badge.model.BadgeInfoUiModel
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
    fun toPresentation(): BadgeInfoUiModel {
        val badge =
            BadgeUiModel(
                id = id,
                imageUrl = badgeImageUrl,
                name = name,
                isAcquired = acquired,
            )

        return BadgeInfoUiModel(
            badge = badge,
            description = description,
            achievedRate = achievedRate.toInt(),
            achievedAt = achievedAt?.date?.toJavaLocalDate(),
            progressRate = progressRate,
        )
    }
}
