package com.yagubogu.data.dto.response.talks

import com.yagubogu.presentation.livetalk.chat.LivetalkChatItem
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class ContentDto(
    @SerialName("id")
    val id: Int, // 톡 메시지 ID
    @SerialName("memberId")
    val memberId: Int, // 메시지를 작성한 멤버의 ID
    @SerialName("nickname")
    val nickname: String, // 작성자의 닉네임
    @SerialName("favorite")
    val favorite: String, // 작성자의 응원팀 이름
    @SerialName("content")
    val content: String, // 채팅 메시지 본문
    @SerialName("createdAt")
    val createdAt: String, // 메시지 작성 시간 (ISO 8601 형식) 예: "2025-07-30T12:00:00"
) {
    // Todo : isMine과 profileImageUrl 정상화 필요
    fun toPresentation(): LivetalkChatItem =
        LivetalkChatItem(
            chatId = id.toLong(),
            isMine = false,
            message = content,
            profileImageUrl = "TODO()",
            nickname = nickname,
            teamName = favorite,
            timestamp = LocalDateTime.parse(createdAt),
        )
}
