package com.yagubogu.ui.mapper

import com.yagubogu.data.dto.response.stadium.StadiumWeather
import com.yagubogu.data.dto.response.stadium.StadiumWeatherResponse
import com.yagubogu.data.dto.response.stadium.StadiumWithGameDto
import com.yagubogu.data.dto.response.stadium.StadiumsWithGamesResponse
import com.yagubogu.domain.model.Coordinate
import com.yagubogu.domain.model.Latitude
import com.yagubogu.domain.model.Longitude
import com.yagubogu.ui.home.model.StadiumWithGame
import com.yagubogu.ui.home.model.StadiumsWithGames
import com.yagubogu.domain.model.WeatherCondition
import com.yagubogu.ui.livetalk.model.WeatherUiModel

fun StadiumsWithGamesResponse.toUiModel(): StadiumsWithGames = StadiumsWithGames(values = stadiums.map { it.toUiModel() })

fun StadiumWithGameDto.toUiModel(): StadiumWithGame =
    StadiumWithGame(
        name = name,
        coordinate =
            Coordinate(
                latitude = Latitude(latitude),
                longitude = Longitude(longitude),
            ),
        gameIds = games.map { it.gameId },
    )

fun StadiumWeatherResponse.toUiModel(): List<WeatherUiModel> =
    data.mapNotNull { stadiumWeather: StadiumWeather ->
        val weatherCondition: WeatherCondition = WeatherCondition.from(stadiumWeather.weather.condition)
        if (weatherCondition == WeatherCondition.UNKNOWN) return@mapNotNull null

        WeatherUiModel(
            stadiumId = stadiumWeather.id.toLong(),
            condition = weatherCondition,
            temperatureText = stadiumWeather.weather.temperature,
        )
    }
