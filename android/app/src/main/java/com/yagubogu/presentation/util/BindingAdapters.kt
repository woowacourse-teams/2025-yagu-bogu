package com.yagubogu.presentation.util

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.yagubogu.R
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

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

@BindingAdapter("timeStamp")
fun setTimeStamp(
    textView: TextView,
    timestamp: LocalDateTime?,
) {
    if (timestamp == null) {
        textView.text = ""
        return
    }
    // Todo: 서버에서 보내주는 타임존 백엔드 합의 필요...?
    val serverTime = timestamp.atZone(ZoneId.of("GMT+0"))
    val localTime = serverTime.withZoneSameInstant(ZoneId.systemDefault())

    textView.text = localTime.toLocalDateTime().format(DateFormatter.amPmhhmm)
}

@BindingAdapter("userProfileImage")
fun ImageView.loadImage(url: String?) {
    if (url.isNullOrEmpty()) {
        setImageResource(R.drawable.ic_user)
        imageTintList = context.getColorStateList(R.color.gray300)
    } else {
        imageTintList = null
        Glide
            .with(this.context)
            .load(url)
            .placeholder(R.drawable.ic_user)
            .circleCrop()
            .into(this)
    }
}
