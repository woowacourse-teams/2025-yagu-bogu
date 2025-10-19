package com.yagubogu.presentation.home.model

import com.yagubogu.domain.model.Coordinate

data class Stadium(
    val shortName: String,
    val location: String,
    val coordinate: Coordinate,
    val games: List<Long>,
)
