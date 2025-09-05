package com.yagubogu.presentation.dialog

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DefaultDialogUiModel(
    val title: String,
    val emoji: String? = null,
    val message: String? = null,
    val negativeText: String? = null,
    val positiveText: String? = null,
) : Parcelable
