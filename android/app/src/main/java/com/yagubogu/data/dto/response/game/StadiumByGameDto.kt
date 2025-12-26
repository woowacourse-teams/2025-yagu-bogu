package com.yagubogu.data.dto.response.game

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StadiumByGameDto(
    @SerialName("name")
    val name: String, // 경기장 이름
)
