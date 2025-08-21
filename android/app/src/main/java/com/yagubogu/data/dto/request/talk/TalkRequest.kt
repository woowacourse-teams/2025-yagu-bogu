package com.yagubogu.data.dto.request.talk

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TalkRequest(
    @SerialName("content")
    val content: String, // 전송할 메시지 내용
)
