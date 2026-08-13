package com.yagubogu.data.dto.response.stadium

import kotlinx.serialization.Serializable

@Serializable
data class StadiumWeather(
    val id: Int,
    val name: String,
    val lat: Double,
    val lng: Double,
    val weather: Weather,
)
