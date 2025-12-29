package com.yagubogu.data.dto.response.talk

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TalkResponse(
    @SerialName("id")
    val id: Long, // 톡 메시지 ID
    @SerialName("memberId")
    val memberId: Long, // 메시지를 작성한 멤버의 ID
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
)
