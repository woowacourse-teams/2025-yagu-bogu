package com.yagubogu.presentation.util

import android.content.res.ColorStateList
import android.view.View
import androidx.databinding.BindingAdapter
import androidx.core.view.ViewCompat

@BindingAdapter("app:backgroundTint")
fun setBackgroundTint(view: View, colorRes: Int?) {
    if (colorRes != null) {
        ViewCompat.setBackgroundTintList(view, ColorStateList.valueOf(view.context.getColor(colorRes)))
    } else {
        ViewCompat.setBackgroundTintList(view, null)
    }
}
