package com.yagubogu.data.dto.request.presigned

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PresignedUrlCompleteRequest(
    @SerialName("key")
    val key: String, // 업로드 완료된 S3 객체 키
)
