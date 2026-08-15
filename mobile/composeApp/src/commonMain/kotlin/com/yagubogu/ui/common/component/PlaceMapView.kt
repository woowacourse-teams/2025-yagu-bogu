package com.yagubogu.ui.common.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun PlaceMapView(
    address: String,
    placeName: String,
    latitude: Double,
    longitude: Double,
    modifier: Modifier = Modifier,
)
