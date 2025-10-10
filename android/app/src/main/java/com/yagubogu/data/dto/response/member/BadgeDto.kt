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
    val id: Long, // 배지 ID
    @SerialName("name")
    val name: String, // 배지 이름
    @SerialName("description")
    val description: String, // 배지 설명
    @SerialName("acquired")
    val acquired: Boolean, // 획득 여부
    @SerialName("achievedAt")
    val achievedAt: LocalDateTime?, // 획득한 날짜 및 시간
    @SerialName("badgeImageUrl")
    val badgeImageUrl: String, // 배지 이미지 주소
    @SerialName("progressRate")
    val progressRate: Double, // 진행률(개인 사용자)
    @SerialName("achievedRate")
    val achievedRate: Double, // 획득률(모든 사용자 기반)
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
