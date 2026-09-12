package com.yagubogu.data.dto.response.place

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PlaceDto(
    @SerialName("id")
    val id: Long,
    @SerialName("category")
    val category: PlaceCategoryDto,
    @SerialName("title")
    val title: String,
    @SerialName("address")
    val address: String? = null,
    @SerialName("mapX")
    val mapX: Double,
    @SerialName("mapY")
    val mapY: Double,
    @SerialName("distance")
    val distance: Int? = null,
    @SerialName("tel")
    val tel: String? = null,
    @SerialName("imageUrl")
    val imageUrl: String? = null,
)
