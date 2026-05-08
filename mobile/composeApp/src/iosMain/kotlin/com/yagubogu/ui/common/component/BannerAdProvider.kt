package com.yagubogu.ui.common.component

import platform.UIKit.UIView

object BannerAdProvider {
    var create: ((adUnitId: String, heightPx: Int) -> UIView)? = null
}
