package com.yagubogu.ui.home.model

import com.yagubogu.domain.model.Coordinate

data class StadiumWithGame(
    val name: String,
    val coordinate: Coordinate,
    val gameIds: List<Long>,
) {
    fun isDoubleHeader(): Boolean = gameIds.size > 1
}
