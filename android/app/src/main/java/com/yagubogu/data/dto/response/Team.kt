package com.yagubogu.data.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Team(
    @SerialName("id")
    val id: Int, // 팀 ID
    @SerialName("name")
    val name: String, // 팀 이름
    @SerialName("occupancyRate")
    val occupancyRate: Double, // 점유율
)
