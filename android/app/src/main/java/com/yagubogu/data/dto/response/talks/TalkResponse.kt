package com.yagubogu.data.dto.response.talks

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TalkResponse(
    @SerialName("stadiumName")
    val stadiumName: String, // 현장톡 채팅 화면의 구장명
    @SerialName("homeTeamName")
    val homeTeamName: String, // 현장톡 채팅 화면의 홈팀명
    @SerialName("awayTeamName")
    val awayTeamName: String, // 현장톡 채팅 화면의 어웨이팀명
    @SerialName("cursorResult")
    val cursorResult: CursorResult, // 페이징징된 톡 메시지
)
