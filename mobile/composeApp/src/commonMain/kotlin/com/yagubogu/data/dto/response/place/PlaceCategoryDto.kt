package com.yagubogu.data.dto.response.place

import kotlinx.serialization.Serializable

@Serializable
enum class PlaceCategoryDto {
    RESTAURANT,
    CAFE,
    LODGING,
    ATTRACTION,
    PERFORMANCE,
}
