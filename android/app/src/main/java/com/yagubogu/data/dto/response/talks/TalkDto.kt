package com.yagubogu.data.dto.response.talks

import com.yagubogu.presentation.livetalk.chat.LivetalkChatItem
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class TalkDto(
    @SerialName("id")
    val id: Int, // 톡 메시지 ID
    @SerialName("memberId")
    val memberId: Int, // 메시지를 작성한 멤버의 ID
    @SerialName("nickname")
    val nickname: String, // 작성자의 닉네임
    @SerialName("favorite")
    val favorite: String, // 작성자의 응원팀 이름
    @SerialName("imageUrl")
    val imageUrl: String, // 작성자의 프로필 이미지 URL
    @SerialName("content")
    val content: String, // 채팅 메시지 본문
    @SerialName("createdAt")
    val createdAt: String, // 메시지 작성 시간 (ISO 8601 형식) 예: "2025-07-30T12:00:00"
    @SerialName("isMine")
    val isMine: Boolean, // 내가 작성한 메시지인지 여부
) {
    fun toPresentation(): LivetalkChatItem =
        LivetalkChatItem(
            chatId = id.toLong(),
            isMine = isMine,
            message = content,
            profileImageUrl = imageUrl,
            nickname = nickname,
            teamName = favorite,
            timestamp = LocalDateTime.parse(createdAt),
            reported = content.contains("숨김처리되었습니다"), // TODO : 추후 백엔드에서 신고된 메시지 보내주는 방식 변경 필요
        )
}
