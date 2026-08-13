package com.yagubogu.ui.util

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes

fun Context.showToast(
    message: String,
    isLong: Boolean = false,
) {
    val duration = if (isLong) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
    Toast.makeText(this, message, duration).show()
}

fun Context.showToast(
    @StringRes message: Int,
    isLong: Boolean = false,
) {
    val duration = if (isLong) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
    Toast.makeText(this, message, duration).show()
}
