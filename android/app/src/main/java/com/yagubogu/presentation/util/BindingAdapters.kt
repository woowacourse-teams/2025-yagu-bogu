package com.yagubogu.presentation.util

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.BindingAdapter
import com.yagubogu.R
import java.time.LocalDate

@BindingAdapter("setCustomChartDividerTint")
fun ImageView.setCustomChartDividerTint(
    @ColorRes colorRes: Int?,
) {
    if (colorRes == null || colorRes == 0) return
    this.imageTintList = context.getColorStateList(colorRes)
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

@BindingAdapter("medalRank")
fun ImageView.setMedalByRank(rank: Int) {
    val (iconRes, tintRes) =
        when (rank) {
            1 -> R.drawable.ic_medal_first to R.color.gold
            2 -> R.drawable.ic_medal_second to R.color.silver
            3 -> R.drawable.ic_medal_third to R.color.bronze
            else -> null
        } ?: run {
            visibility = View.GONE
            return
        }

    visibility = View.VISIBLE
    setImageResource(iconRes)
    imageTintList = context.getColorStateList(tintRes)
}

@BindingAdapter("textColorRes")
fun TextView.setTextColorRes(
    @ColorRes colorRes: Int?,
) {
    if (colorRes == null || colorRes == 0) return
    setTextColor(context.getColor(colorRes))
}

@BindingAdapter("dateFormat")
fun TextView.setDateFormat(date: LocalDate?) {
    date ?: return
    text = date.format(DateFormatter.yyyyMMdd)
}

@BindingAdapter("conditionalMarginStart")
fun View.setConditionalMarginStart(marginStartDp: Int?) {
    val layoutParams = this.layoutParams as? ViewGroup.MarginLayoutParams ?: return

    marginStartDp?.let {
        layoutParams.marginStart = it.dpToPx(context)
    }

    this.layoutParams = layoutParams
}

@BindingAdapter("conditionalMarginEnd")
fun View.setConditionalMargins(marginEndDp: Int?) {
    val layoutParams = this.layoutParams as? ViewGroup.MarginLayoutParams ?: return

    marginEndDp?.let {
        layoutParams.marginEnd = it.dpToPx(context)
    }

    this.layoutParams = layoutParams
}

fun Int.dpToPx(context: Context): Int = (this * context.resources.displayMetrics.density).toInt()
