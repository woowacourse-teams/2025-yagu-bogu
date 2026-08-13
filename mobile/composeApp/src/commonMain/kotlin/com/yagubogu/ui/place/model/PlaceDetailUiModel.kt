package com.yagubogu.ui.place.model

data class PlaceDetailUiModel(
    val id: Long,
    val category: PlaceCategory,
    val name: String,
    val address: String?,
    val latitude: Double,
    val longitude: Double,
    val tel: String?,
    val imageUrl: String?,
    val overview: String?,
    val homepage: String?,
    val distanceMeters: Int?,
    val businessHours: String?,
    val rows: List<PlaceDetailRow>,
)
