package com.yagubogu.presentation.util

import android.content.res.ColorStateList
import android.view.View
import android.widget.ImageView
import androidx.annotation.ColorRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter

@BindingAdapter("setCustomChartDividerTint")
fun ImageView.setCustomChartDividerTint(
    @ColorRes colorRes: Int?,
) {
    colorRes ?: return
    this.imageTintList =
        ColorStateList.valueOf(
            ContextCompat.getColor(context, colorRes),
        )
}

@BindingAdapter("setCustomChartBackground")
fun View.setCustomChartBackground(
    @ColorRes colorRes: Int?,
) {
    colorRes ?: return
    setBackgroundColor(ContextCompat.getColor(context, colorRes))
}

@BindingAdapter("setConstraintWidthPercent")
fun View.setConstraintWidthPercent(percent: Double) {
    val layoutParams = this.layoutParams as? ConstraintLayout.LayoutParams
    layoutParams?.let { params ->
        params.matchConstraintPercentWidth = percent.toFloat()
        this.layoutParams = params
    }
}

@BindingAdapter("setConstraintGuidePercent")
fun View.setConstraintGuidePercent(percent: Double) {
    val layoutParams = this.layoutParams as? ConstraintLayout.LayoutParams
    layoutParams?.let { params ->
        params.guidePercent = percent.toFloat()
        this.layoutParams = params
    }
}
