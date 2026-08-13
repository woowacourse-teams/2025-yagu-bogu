package com.yagubogu.ui.common.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIColor

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun BannerAdView(
    adUnitId: String,
    bannerAdType: BannerAdType,
    modifier: Modifier,
) {
    UIKitView(
        factory = {
            val view =
                BannerAdProvider.create?.invoke(adUnitId, bannerAdType.heightDp)
                    ?: platform.UIKit.UIView()
            view.backgroundColor = UIColor.whiteColor
            view
        },
        modifier =
            modifier
                .fillMaxWidth()
                .height(bannerAdType.heightDp.dp),
        update = {},
        onRelease = {},
        properties =
            UIKitInteropProperties(
                isInteractive = true,
                isNativeAccessibilityEnabled = true,
            ),
    )
}
