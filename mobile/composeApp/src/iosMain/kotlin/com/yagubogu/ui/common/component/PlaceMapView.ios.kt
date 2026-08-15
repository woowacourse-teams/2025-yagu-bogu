package com.yagubogu.ui.common.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIView

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun PlaceMapView(
    address: String,
    placeName: String,
    latitude: Double,
    longitude: Double,
    modifier: Modifier,
) {
    UIKitView(
        factory = {
            PlaceMapViewProvider.create?.invoke(address, placeName, latitude, longitude) ?: UIView()
        },
        modifier = modifier,
        update = { view -> PlaceMapViewProvider.update?.invoke(view, address, placeName, latitude, longitude) },
        onRelease = { view -> PlaceMapViewProvider.dispose?.invoke(view) },
        properties =
            UIKitInteropProperties(
                isInteractive = false,
                isNativeAccessibilityEnabled = true,
            ),
    )
}
