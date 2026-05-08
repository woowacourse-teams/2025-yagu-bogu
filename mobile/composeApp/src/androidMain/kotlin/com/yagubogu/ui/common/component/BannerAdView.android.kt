package com.yagubogu.ui.common.component

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.libraries.ads.mobile.sdk.banner.AdView
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAd
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdEventCallback
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdRequest
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.google.android.libraries.ads.mobile.sdk.banner.AdSize as SdkAdSize

private fun BannerAdType.toSdkAdSize(): SdkAdSize =
    when (this) {
        BannerAdType.BANNER -> SdkAdSize.BANNER
        BannerAdType.LARGE_BANNER -> SdkAdSize.LARGE_BANNER
        BannerAdType.MEDIUM_RECTANGLE -> SdkAdSize.MEDIUM_RECTANGLE
    }

@Composable
actual fun BannerAdView(
    adUnitId: String,
    bannerAdType: BannerAdType,
    modifier: Modifier,
) {
    val activity = LocalActivity.current ?: return
    var bannerAd by remember { mutableStateOf<BannerAd?>(null) }

    DisposableEffect(Unit) {
        onDispose { bannerAd?.destroy() }
    }

    AndroidView(
        modifier = modifier,
        factory = {
            AdView(activity).also { adView ->
                val adRequest =
                    BannerAdRequest.Builder(adUnitId, bannerAdType.toSdkAdSize()).build()
                adView.loadAd(
                    adRequest,
                    object : AdLoadCallback<BannerAd> {
                        override fun onAdLoaded(ad: BannerAd) {
                            bannerAd = ad
                            ad.adEventCallback =
                                object : BannerAdEventCallback {
                                    override fun onAdImpression() = Unit

                                    override fun onAdClicked() = Unit

                                    override fun onAdShowedFullScreenContent() = Unit

                                    override fun onAdDismissedFullScreenContent() = Unit

                                    override fun onAdFailedToShowFullScreenContent(fullScreenContentError: FullScreenContentError) = Unit
                                }
                        }

                        override fun onAdFailedToLoad(adError: LoadAdError) = Unit
                    },
                )
            }
        },
    )
}
