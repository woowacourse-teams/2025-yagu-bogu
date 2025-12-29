package com.yagubogu.data.dto.response.talk

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// TODO 반영되면 수정해야 함
@Serializable
data class TalkEntranceResponse(
    @SerialName("stadiumName")
    val stadiumName: String, // 경기장 이름
    @SerialName("homeTeamCode")
    val homeTeamCode: String, // 홈팀 코드
    @SerialName("awayTeamCode")
    val awayTeamCode: String, // 어웨이팀 코드
    @SerialName("myTeamCode")
    val myTeamCode: String, // 내 팀 코드
)
