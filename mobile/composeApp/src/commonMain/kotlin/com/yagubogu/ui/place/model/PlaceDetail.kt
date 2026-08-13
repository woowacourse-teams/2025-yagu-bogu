package com.yagubogu.ui.place.model

import org.jetbrains.compose.resources.StringResource

data class PlaceDetailRow(
    val labelRes: StringResource,
    val value: String,
)

sealed interface PlaceDetail {
    val businessHours: String?
    val rows: List<PlaceDetailRow>

    data class FoodDetail(
        override val businessHours: String?,
        override val rows: List<PlaceDetailRow>,
    ) : PlaceDetail

    data class AttractionDetail(
        override val businessHours: String?,
        override val rows: List<PlaceDetailRow>,
    ) : PlaceDetail

    data class LodgingDetail(
        override val businessHours: String?,
        override val rows: List<PlaceDetailRow>,
    ) : PlaceDetail

    data class PerformanceDetail(
        override val businessHours: String?,
        override val rows: List<PlaceDetailRow>,
    ) : PlaceDetail
}
