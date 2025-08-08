package com.yagubogu.data.dto.response.talks

import com.yagubogu.presentation.livetalk.chat.LivetalkCursorItem
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CursorResult(
    @SerialName("content")
    val contents: List<ContentDto>, // 페이징된 톡 메시지 리스트
    @SerialName("nextCursorId")
    val nextCursorId: Long?, // 다음 페이지 조회를 위한 커서 ID (없으면 null)
    @SerialName("hasNext")
    val hasNext: Boolean, // 다음 페이지가 존재하는지 여부
) {
    fun toPresentation(): LivetalkCursorItem =
        LivetalkCursorItem(
            chats = contents.map { it.toPresentation() },
            nextCursorId = nextCursorId,
            hasNext = hasNext,
        )
}
