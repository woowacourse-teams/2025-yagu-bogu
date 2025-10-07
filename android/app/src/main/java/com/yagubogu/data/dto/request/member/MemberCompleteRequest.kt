package com.yagubogu.data.dto.request.member

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MemberCompleteRequest(
    @SerialName("key")
    val key: String                // 업로드 완료된 S3 객체 키
)