package com.yagubogu.data.dto.response.member

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RepresentativeBadgeDto(
    @SerialName("id")
    val id: Long, // 대표 배지 ID
    @SerialName("name")
    val name: String, // 대표 배지 이름
    @SerialName("badgeImageUrl")
    val badgeImageUrl: String, // 대표 배지 이미지 주소
)
