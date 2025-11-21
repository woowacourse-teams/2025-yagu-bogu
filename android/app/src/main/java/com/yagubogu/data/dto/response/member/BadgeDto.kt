package com.yagubogu.data.dto.response.member

import kotlinx.datetime.LocalDateTime
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
)
