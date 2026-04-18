package com.yagubogu.domain.model

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

    fun findNearestTo(
        coordinate: Coordinate,
        threshold: Distance,
        getDistance: (Coordinate, Coordinate) -> Distance,
    ): Pair<StadiumWithGame, Distance>? =
        findNearestTo(coordinate, getDistance)
            ?.takeIf { (_, distance) -> distance.isWithin(threshold) }
}
