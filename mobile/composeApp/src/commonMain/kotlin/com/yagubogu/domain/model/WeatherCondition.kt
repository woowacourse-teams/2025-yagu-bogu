package com.yagubogu.domain.model

enum class WeatherCondition {
    THUNDERSTORM,
    HEAVY_RAIN,
    LIGHT_RAIN,
    RAIN_SNOW,
    SNOW,
    STRONG_WIND,
    CLEAR,
    PARTLY_CLOUDY,
    CLOUDY,
    UNKNOWN,
    ;

    companion object {
        fun from(value: String): WeatherCondition = entries.find { it.name == value } ?: UNKNOWN
    }
}
