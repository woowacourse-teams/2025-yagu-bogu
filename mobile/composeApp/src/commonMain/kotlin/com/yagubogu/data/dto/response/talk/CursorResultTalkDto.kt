package com.yagubogu.data.dto.response.talk

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CursorResultTalkDto(
    @SerialName("content")
    val contents: List<TalkResponse>, // 페이징된 톡 메시지 리스트
    @SerialName("nextCursorId")
    val nextCursorId: Long?, // 다음 페이지 조회를 위한 커서 ID (없으면 null)
    @SerialName("hasNext")
    val hasNext: Boolean, // 다음 페이지가 존재하는지 여부
)
