package com.yagubogu.ui.util

import androidx.annotation.StringRes
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.skydoves.balloon.ArrowOrientation
import com.skydoves.balloon.ArrowOrientationRules
import com.skydoves.balloon.Balloon
import com.skydoves.balloon.compose.rememberBalloonBuilder
import com.yagubogu.R
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@Composable
fun rememberNoRippleInteractionSource(): MutableInteractionSource =
    remember {
        object : MutableInteractionSource {
            override val interactions: Flow<Interaction> = emptyFlow()

            override suspend fun emit(interaction: Interaction) {}

            override fun tryEmit(interaction: Interaction) = true
        }
    }

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
