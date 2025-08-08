package com.yagubogu.data.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TalksRequest(
    @SerialName("content")
    val content: String, // 전송할 메시지 내용
)
