package com.yagubogu.data.dto.response.place

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PlacesResponse(
    @SerialName("stadiumId")
    val stadiumId: Long,
    @SerialName("category")
    val category: PlaceCategoryDto,
    @SerialName("places")
    val places: List<PlaceDto>,
)
