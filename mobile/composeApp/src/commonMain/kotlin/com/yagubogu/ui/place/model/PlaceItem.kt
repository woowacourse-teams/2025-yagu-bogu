package com.yagubogu.ui.place.model

import androidx.compose.ui.graphics.Color

data class PlaceItem(
    val category: PlaceCategory,
    val name: String,
    val distance: String,
    val rating: String,
    val reviewCount: Int,
    val thumbnailLabel: String,
    val thumbnailColor: Color,
)
