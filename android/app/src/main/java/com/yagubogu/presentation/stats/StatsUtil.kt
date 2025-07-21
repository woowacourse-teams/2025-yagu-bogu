package com.yagubogu.presentation.stats

import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan

fun String.toStatsCenterSpannableString(
    primaryColor: Int,
    secondaryColor: Int,
    firstLineSize: Float,
    secondLineSize: Float,
    firstTf: Typeface? = null,
    secondTf: Typeface? = null,
): SpannableString {
    val newline = indexOf('\n').coerceAtLeast(0)
    val firstStart = 0
    val firstEnd = newline
    val secondStart = (newline + 1).coerceAtMost(length)
    val secondEnd = length

    val sp = SpannableString(this)

    sp.setSpan(RelativeSizeSpan(firstLineSize), firstStart, firstEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    sp.setSpan(ForegroundColorSpan(primaryColor), firstStart, firstEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    firstTf?.let { sp.setSpan(CustomTypefaceSpan(it), firstStart, firstEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE) }

    sp.setSpan(RelativeSizeSpan(secondLineSize), secondStart, secondEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    sp.setSpan(ForegroundColorSpan(secondaryColor), secondStart, secondEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    secondTf?.let { sp.setSpan(CustomTypefaceSpan(it), secondStart, secondEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE) }

    return sp
}
