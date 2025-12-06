package com.yagubogu.data.dto.response.checkin

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CheckInGameTeamDto(
    @SerialName("code")
    val code: String, // 팀 코드
    @SerialName("name")
    val name: String, // 팀 이름
    @SerialName("score")
    val score: Int?, // 팀 점수
    @SerialName("isMyTeam")
    val isMyTeam: Boolean, // 내가 응원하는 팀 여부
    @SerialName("pitcher")
    val pitcher: String?, // 투수 이름
)
