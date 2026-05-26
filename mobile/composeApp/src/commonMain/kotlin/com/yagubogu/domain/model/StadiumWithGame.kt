package com.yagubogu.domain.model

data class StadiumWithGame(
    val name: String,
    val coordinate: Coordinate,
    val gameIds: List<Long>,
) {
    fun isDoubleHeader(): Boolean = gameIds.size > 1
}
