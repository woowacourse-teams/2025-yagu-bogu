package com.yagubogu.data.dto.response.checkin

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CheckInImageDto(
    @SerialName("imageId")
    val imageId: Long, // 이미지 ID
    @SerialName("imageUrl")
    val imageUrl: String, // 이미지 URL
)
