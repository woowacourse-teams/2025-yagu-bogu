package com.yagubogu.ui.util

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import com.skydoves.balloon.ArrowOrientation
import com.skydoves.balloon.ArrowOrientationRules
import com.skydoves.balloon.Balloon
import com.skydoves.balloon.compose.rememberBalloonBuilder
import com.yagubogu.R

@Composable
fun rememberBalloonBuilder(
    @StringRes textResId: Int,
): Balloon.Builder =
    rememberBalloonBuilder {
        setTextResource(textResId)
        setWidthRatio(0.5f)
        setCornerRadius(8f)
        setPaddingHorizontal(10)
        setPaddingVertical(8)
        setTextColorResource(R.color.gray800)
        setBackgroundColorResource(R.color.gray200)
        setArrowTopPadding(4)
        setArrowOrientation(ArrowOrientation.TOP)
        setArrowOrientationRules(ArrowOrientationRules.ALIGN_FIXED)
    }
