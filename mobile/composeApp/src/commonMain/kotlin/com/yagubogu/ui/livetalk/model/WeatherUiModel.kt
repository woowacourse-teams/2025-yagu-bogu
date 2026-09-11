package com.yagubogu.ui.livetalk.model

import com.yagubogu.domain.model.WeatherCondition

data class WeatherUiModel(
    val stadiumId: Long,
    val condition: WeatherCondition,
    val temperatureText: String,
)
