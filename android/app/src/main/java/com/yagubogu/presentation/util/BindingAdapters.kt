package com.yagubogu.presentation.util

import android.content.res.ColorStateList
import android.view.View
import androidx.annotation.ColorRes
import androidx.databinding.BindingAdapter

@BindingAdapter("app:legendBackgroundTint")
fun View.setLegendBackgroundTint(
    @ColorRes
    colorRes: Int?,
) {
    colorRes ?: return
    this.backgroundTintList = ColorStateList.valueOf(context.getColor(colorRes))
}
