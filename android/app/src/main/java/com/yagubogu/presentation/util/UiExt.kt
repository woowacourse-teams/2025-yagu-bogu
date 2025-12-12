package com.yagubogu.presentation.util

import android.content.Context
import android.graphics.Color
import android.view.View
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import com.google.android.material.snackbar.Snackbar
import com.yagubogu.R

fun View.showSnackbar(
    message: String,
    @IdRes anchorViewId: Int? = null,
) {
    Snackbar.make(this, message, Snackbar.LENGTH_SHORT).apply {
        setBackgroundTint(Color.DKGRAY)
        setTextColor(context.getColor(R.color.white))
        anchorViewId?.let { setAnchorView(it) }
        show()
    }
}

fun View.showSnackbar(
    @StringRes message: Int,
    @IdRes anchorViewId: Int? = null,
) {
    Snackbar.make(this, message, Snackbar.LENGTH_SHORT).apply {
        setBackgroundTint(Color.DKGRAY)
        setTextColor(context.getColor(R.color.white))
        anchorViewId?.let { setAnchorView(it) }
        show()
    }
}

fun Context.showToast(
    message: String,
    isLong: Boolean = false,
) {
    val duration = if (isLong) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
    Toast.makeText(this, message, duration).show()
}

fun Context.showToast(
    @StringRes message: Int,
) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}
