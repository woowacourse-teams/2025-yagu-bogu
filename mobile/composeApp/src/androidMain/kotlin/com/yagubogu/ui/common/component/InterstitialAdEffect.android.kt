package com.yagubogu.ui.common.component

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.AdRequest
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.google.android.libraries.ads.mobile.sdk.interstitial.InterstitialAd
import com.google.android.libraries.ads.mobile.sdk.interstitial.InterstitialAdEventCallback
import kotlinx.coroutines.flow.Flow

@Composable
actual fun InterstitialAdEffect(
    triggerFlow: Flow<Unit>,
    adUnitId: String,
    onAdComplete: () -> Unit,
) {
    val activity = LocalActivity.current ?: return

    var loadedAd by remember { mutableStateOf<InterstitialAd?>(null) }
    val currentOnAdComplete by rememberUpdatedState(onAdComplete)

    DisposableEffect(Unit) {
        onDispose { loadedAd?.destroy() }
    }

    fun loadAd() {
        InterstitialAd.load(
            AdRequest.Builder(adUnitId).build(),
            object : AdLoadCallback<InterstitialAd> {
                override fun onAdLoaded(ad: InterstitialAd) {
                    loadedAd = ad
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    loadedAd = null
                }
            },
        )
    }

    LaunchedEffect(adUnitId) {
        loadedAd?.destroy()
        loadedAd = null
        loadAd()
    }

    LaunchedEffect(triggerFlow) {
        triggerFlow.collect {
            val ad = loadedAd
            if (ad == null) {
                currentOnAdComplete()
                return@collect
            }
            ad.adEventCallback =
                object : InterstitialAdEventCallback {
                    override fun onAdDismissedFullScreenContent() {
                        loadedAd = null
                        loadAd()
                        currentOnAdComplete()
                    }

                    override fun onAdFailedToShowFullScreenContent(fullScreenContentError: FullScreenContentError) {
                        loadedAd = null
                        loadAd()
                        currentOnAdComplete()
                    }

                    override fun onAdShowedFullScreenContent() = Unit

                    override fun onAdImpression() = Unit

                    override fun onAdClicked() = Unit
                }
            loadedAd = null
            ad.show(activity)
        }
    }
}
