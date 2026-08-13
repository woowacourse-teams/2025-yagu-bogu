package com.yagubogu.ui.place.model

data class PlaceItem(
    val id: Long,
    val category: PlaceCategory,
    val name: String,
    val distance: String,
    val imageUrl: String?,
)
