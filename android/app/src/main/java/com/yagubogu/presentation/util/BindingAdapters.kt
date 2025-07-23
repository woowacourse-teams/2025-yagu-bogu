package com.yagubogu.presentation.util

import android.content.res.ColorStateList
import android.view.View
import androidx.core.view.ViewCompat
import androidx.databinding.BindingAdapter

@BindingAdapter("app:backgroundTint")
fun setBackgroundTint(
    view: View,
    colorRes: Int?,
) {
    if (colorRes != null) {
        ViewCompat.setBackgroundTintList(view, ColorStateList.valueOf(view.context.getColor(colorRes)))
    }
}
