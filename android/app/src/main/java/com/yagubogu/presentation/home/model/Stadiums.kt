package com.yagubogu.presentation.home.model

import com.yagubogu.domain.model.Coordinate
import com.yagubogu.domain.model.Distance

data class Stadiums(
    val values: List<Stadium>,
) {
    fun isEmpty(): Boolean = values.isEmpty()

    fun findNearestTo(
        coordinate: Coordinate,
        getDistance: (Coordinate, Coordinate) -> Distance,
    ): Pair<Stadium, Distance>? =
        values
            .map { stadium: Stadium ->
                val distance = getDistance(coordinate, stadium.coordinate)
                stadium to distance
            }.minByOrNull { it.second.value }
}
