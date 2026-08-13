package com.yagubogu.ui.util

import androidx.compose.ui.graphics.Color
import com.yagubogu.ui.place.model.PlaceCategory

val PlaceCategory.categoryThumbnailColor: Color
    get() =
        when (this) {
            PlaceCategory.FOOD -> Color(0xFFE8A444)
            PlaceCategory.CAFE -> Color(0xFF6B8F71)
            PlaceCategory.STAY -> Color(0xFF4A6FA5)
            PlaceCategory.TOUR -> Color(0xFF8F5A3C)
            PlaceCategory.SHOW -> Color(0xFFB05A9C)
        }
