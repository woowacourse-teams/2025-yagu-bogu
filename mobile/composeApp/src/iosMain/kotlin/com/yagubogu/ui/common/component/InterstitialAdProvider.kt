package com.yagubogu.ui.common.component

object InterstitialAdProvider {
    var preload: ((adUnitId: String) -> Unit)? = null
    var show: ((adUnitId: String, onComplete: () -> Unit) -> Unit)? = null
}
