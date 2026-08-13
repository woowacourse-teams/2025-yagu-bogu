package com.yagubogu.data.dto.response.stadium

import kotlinx.serialization.Serializable

@Serializable
data class Weather(
    val condition: String,
    val sky: String,
    val temperature: String,
    val precipitation: String,
    val windSpeed: String,
)
