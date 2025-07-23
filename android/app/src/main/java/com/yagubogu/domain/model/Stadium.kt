package com.yagubogu.domain.model

data class Stadium(
    val id: Long,
    val fullName: String,
    val shortName: String,
    val location: String,
    val coordinate: Coordinate,
)
