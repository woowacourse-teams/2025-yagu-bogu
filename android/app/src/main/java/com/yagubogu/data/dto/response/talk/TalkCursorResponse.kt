package com.yagubogu.data.dto.response.talk

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TalkCursorResponse(
    @SerialName("cursorResult")
    val cursorResult: CursorResultTalkDto, // 페이징된 톡 메시지
)
