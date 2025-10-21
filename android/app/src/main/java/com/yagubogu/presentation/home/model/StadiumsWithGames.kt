package com.yagubogu.presentation.home.model

import com.yagubogu.domain.model.Coordinate
import com.yagubogu.domain.model.Distance

data class StadiumsWithGames(
    val values: List<StadiumWithGame>,
) {
    fun isEmpty(): Boolean = values.isEmpty()

    fun findNearestTo(
        coordinate: Coordinate,
        getDistance: (Coordinate, Coordinate) -> Distance,
    ): Pair<StadiumWithGame, Distance>? =
        values
            .map { stadium: StadiumWithGame ->
                val distance = getDistance(coordinate, stadium.coordinate)
                stadium to distance
            }.minByOrNull { it.second.value }
}
