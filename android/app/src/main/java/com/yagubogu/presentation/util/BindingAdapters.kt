package com.yagubogu.presentation.util

import android.content.res.ColorStateList
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.BindingAdapter

@BindingAdapter("setCustomChartDividerTint")
fun ImageView.setCustomChartDividerTint(
    @ColorRes colorRes: Int?,
) {
    if (colorRes == null || colorRes == 0) return
    this.imageTintList = ColorStateList.valueOf(context.getColor(colorRes))
}

@BindingAdapter("setCustomChartBackground")
fun View.setCustomChartBackground(
    @ColorRes colorRes: Int?,
) {
    if (colorRes == null || colorRes == 0) return
    setBackgroundColor(context.getColor(colorRes))
}

@BindingAdapter("setConstraintWidthPercent")
fun View.setConstraintWidthPercent(percent: Double) {
    val layoutParams = this.layoutParams as? ConstraintLayout.LayoutParams ?: return
    layoutParams.matchConstraintPercentWidth = percent.toFloat()
    this.layoutParams = layoutParams
}

@BindingAdapter("setConstraintGuidePercent")
fun View.setConstraintGuidePercent(percent: Double) {
    val layoutParams = this.layoutParams as? ConstraintLayout.LayoutParams ?: return
    layoutParams.guidePercent = percent.toFloat()
    this.layoutParams = layoutParams
}

@BindingAdapter("textColorRes")
fun TextView.setTextColorRes(
    @ColorRes colorRes: Int?,
) {
    if (colorRes == null || colorRes == 0) return
    setTextColor(context.getColor(colorRes))
}
