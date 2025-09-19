package com.yagubogu.data.dto.response.talk

import com.yagubogu.presentation.livetalk.chat.LivetalkResponseItem
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TalkCursorResponse(
    @SerialName("stadiumName")
    val stadiumName: String, // 현장톡 채팅 화면의 구장명
    @SerialName("homeTeamName")
    val homeTeamName: String, // 현장톡 채팅 화면의 홈팀명
    @SerialName("awayTeamName")
    val awayTeamName: String, // 현장톡 채팅 화면의 어웨이팀명
    @SerialName("cursorResult")
    val cursorResult: CursorResultTalkDto, // 페이징된 톡 메시지
) {
    fun toPresentation(): LivetalkResponseItem =
        LivetalkResponseItem(
            stadiumName = stadiumName,
            homeTeamName = homeTeamName,
            awayTeamName = awayTeamName,
            cursor = cursorResult.toPresentation(),
        )
}
