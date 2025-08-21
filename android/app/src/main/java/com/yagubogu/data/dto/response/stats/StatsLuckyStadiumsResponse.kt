package com.yagubogu.data.dto.response.stats

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StatsLuckyStadiumsResponse(
    @SerialName("shortName")
    val shortName: String?, // 구장 별명, 행운의 구장이 없다면 null
)
