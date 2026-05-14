package com.yagubogu.ui.livetalk.model

import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.ic_weather_clear
import yagubogu.composeapp.generated.resources.ic_weather_cloudy
import yagubogu.composeapp.generated.resources.ic_weather_heavy_rain
import yagubogu.composeapp.generated.resources.ic_weather_light_rain
import yagubogu.composeapp.generated.resources.ic_weather_partly_cloudy
import yagubogu.composeapp.generated.resources.ic_weather_rain_snow
import yagubogu.composeapp.generated.resources.ic_weather_snow
import yagubogu.composeapp.generated.resources.ic_weather_strong_wind
import yagubogu.composeapp.generated.resources.ic_weather_thunderstorm
import yagubogu.composeapp.generated.resources.livetalk_weather_type_clear
import yagubogu.composeapp.generated.resources.livetalk_weather_type_cloudy
import yagubogu.composeapp.generated.resources.livetalk_weather_type_heavy_rain
import yagubogu.composeapp.generated.resources.livetalk_weather_type_light_rain
import yagubogu.composeapp.generated.resources.livetalk_weather_type_partly_cloudy
import yagubogu.composeapp.generated.resources.livetalk_weather_type_rain_snow
import yagubogu.composeapp.generated.resources.livetalk_weather_type_snow
import yagubogu.composeapp.generated.resources.livetalk_weather_type_strong_wind
import yagubogu.composeapp.generated.resources.livetalk_weather_type_thunderstorm

data class WeatherUiModel(
    val stadiumId: Long,
    val condition: Condition,
    val temperatureText: String,
)

sealed class Condition {
    data object Thunderstorm : Condition()

    data object HeavyRain : Condition()

    data object LightRain : Condition()

    data object RainSnow : Condition()

    data object Snow : Condition()

    data object StrongWind : Condition()

    data object Clear : Condition()

    data object PartlyCloudy : Condition()

    data object Cloudy : Condition()

    companion object {
        fun from(value: String): Condition =
            when (value) {
                "THUNDERSTORM" -> Thunderstorm
                "HEAVY_RAIN" -> HeavyRain
                "LIGHT_RAIN" -> LightRain
                "RAIN_SNOW" -> RainSnow
                "SNOW" -> Snow
                "STRONG_WIND" -> StrongWind
                "CLEAR" -> Clear
                "PARTLY_CLOUDY" -> PartlyCloudy
                "CLOUDY" -> Cloudy
                else -> Clear // fallback
            }
    }
}

fun Condition.toResource(): DrawableResource =
    when (this) {
        Condition.Clear -> Res.drawable.ic_weather_clear
        Condition.Cloudy -> Res.drawable.ic_weather_cloudy
        Condition.HeavyRain -> Res.drawable.ic_weather_heavy_rain
        Condition.LightRain -> Res.drawable.ic_weather_light_rain
        Condition.PartlyCloudy -> Res.drawable.ic_weather_partly_cloudy
        Condition.RainSnow -> Res.drawable.ic_weather_rain_snow
        Condition.Snow -> Res.drawable.ic_weather_snow
        Condition.StrongWind -> Res.drawable.ic_weather_strong_wind
        Condition.Thunderstorm -> Res.drawable.ic_weather_thunderstorm
    }

fun Condition.toStringResource(): StringResource =
    when (this) {
        Condition.Clear -> Res.string.livetalk_weather_type_clear
        Condition.Cloudy -> Res.string.livetalk_weather_type_cloudy
        Condition.HeavyRain -> Res.string.livetalk_weather_type_heavy_rain
        Condition.LightRain -> Res.string.livetalk_weather_type_light_rain
        Condition.PartlyCloudy -> Res.string.livetalk_weather_type_partly_cloudy
        Condition.RainSnow -> Res.string.livetalk_weather_type_rain_snow
        Condition.Snow -> Res.string.livetalk_weather_type_snow
        Condition.StrongWind -> Res.string.livetalk_weather_type_strong_wind
        Condition.Thunderstorm -> Res.string.livetalk_weather_type_thunderstorm
    }
