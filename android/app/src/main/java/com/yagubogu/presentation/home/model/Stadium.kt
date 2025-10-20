package com.yagubogu.presentation.home.model

import com.yagubogu.domain.model.Coordinate

data class Stadium(
    val name: String,
    val coordinate: Coordinate,
    val gameIds: List<Long>,
) {
    fun isDoubleHeader(): Boolean = gameIds.size > 1
}
