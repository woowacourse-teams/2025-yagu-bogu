package com.yagubogu.ui.place.model

import org.jetbrains.compose.resources.StringResource
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.place_category_cafe
import yagubogu.composeapp.generated.resources.place_category_food
import yagubogu.composeapp.generated.resources.place_category_show
import yagubogu.composeapp.generated.resources.place_category_stay
import yagubogu.composeapp.generated.resources.place_category_tour

enum class PlaceCategory {
    FOOD,
    STAY,
    TOUR,
    SHOW,
    CAFE,
}

val PlaceCategory.labelResource: StringResource
    get() =
        when (this) {
            PlaceCategory.FOOD -> Res.string.place_category_food
            PlaceCategory.STAY -> Res.string.place_category_stay
            PlaceCategory.TOUR -> Res.string.place_category_tour
            PlaceCategory.SHOW -> Res.string.place_category_show
            PlaceCategory.CAFE -> Res.string.place_category_cafe
        }
