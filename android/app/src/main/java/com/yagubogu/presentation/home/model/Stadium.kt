package com.yagubogu.presentation.home.model

import com.yagubogu.domain.model.Coordinate

data class Stadium(
    val id: Long,
    val fullName: String,
    val shortName: String,
    val location: String,
    val coordinate: Coordinate,
)
