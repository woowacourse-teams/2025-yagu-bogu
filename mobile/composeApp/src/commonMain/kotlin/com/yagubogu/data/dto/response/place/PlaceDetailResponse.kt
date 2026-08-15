package com.yagubogu.data.dto.response.place

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class PlaceDetailResponse(
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
    @SerialName("tel")
    val tel: String? = null,
    @SerialName("imageUrl")
    val imageUrl: String? = null,
    @SerialName("overview")
    val overview: String? = null,
    @SerialName("homepage")
    val homepage: String? = null,
    @SerialName("detail")
    val detail: JsonObject? = null,
)
