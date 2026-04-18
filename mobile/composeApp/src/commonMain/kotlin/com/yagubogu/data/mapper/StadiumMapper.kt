package com.yagubogu.data.mapper

import com.yagubogu.data.dto.response.stadium.StadiumWithGameDto
import com.yagubogu.data.dto.response.stadium.StadiumsWithGamesResponse
import com.yagubogu.domain.model.Coordinate
import com.yagubogu.domain.model.Latitude
import com.yagubogu.domain.model.Longitude
import com.yagubogu.domain.model.StadiumWithGame
import com.yagubogu.domain.model.StadiumsWithGames

fun StadiumsWithGamesResponse.toDomain(): StadiumsWithGames = StadiumsWithGames(values = stadiums.map { it.toDomain() })

fun StadiumWithGameDto.toDomain(): StadiumWithGame =
    StadiumWithGame(
        name = name,
        coordinate =
            Coordinate(
                latitude = Latitude(latitude),
                longitude = Longitude(longitude),
            ),
        gameIds = games.map { it.gameId },
    )
