package com.yagubogu.data.dto.request.checkin

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CheckInImageRequest(
    @SerialName("imageKey")
    val imageKey: String, // S3에 업로드된 이미지 키
)
