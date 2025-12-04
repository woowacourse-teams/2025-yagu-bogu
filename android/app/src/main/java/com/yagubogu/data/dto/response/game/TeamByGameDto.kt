package com.yagubogu.data.dto.response.game

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TeamByGameDto(
    @SerialName("id")
    val id: Int, // 팀 식별자
    @SerialName("name")
    val name: String, // 팀 이름
    @SerialName("code")
    val code: String, // 팀 코드
)
