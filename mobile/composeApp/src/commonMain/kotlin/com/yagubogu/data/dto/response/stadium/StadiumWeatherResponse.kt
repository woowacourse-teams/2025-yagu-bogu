package com.yagubogu.data.dto.response.stadium

import kotlinx.serialization.Serializable

@Serializable
data class StadiumWeatherResponse(
    val success: Boolean,
    val count: Int,
    val ncstBaseDate: String,
    val ncstBaseTime: String,
    val fcstBaseDate: String,
    val fcstBaseTime: String,
    val data: List<StadiumWeather>,
)
