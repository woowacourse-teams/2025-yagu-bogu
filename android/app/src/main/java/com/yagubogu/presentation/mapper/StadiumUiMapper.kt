package com.yagubogu.presentation.mapper

import com.yagubogu.data.dto.response.stadium.StadiumWithGameDto
import com.yagubogu.data.dto.response.stadium.StadiumsWithGamesResponse
import com.yagubogu.domain.model.Coordinate
import com.yagubogu.domain.model.Latitude
import com.yagubogu.domain.model.Longitude
import com.yagubogu.presentation.home.model.StadiumWithGame
import com.yagubogu.presentation.home.model.StadiumsWithGames

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
