package com.yagubogu.data.dto.response.member

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RepresentativeBadgeDto(
    @SerialName("name")
    val name: String, // 대표 배지 이름
    @SerialName("imageUrl")
    val imageUrl: String, // 대표 배지 이미지 주소
)
