package com.yagubogu.presentation.dialog

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DefaultDialogUiModel(
    val emoji: String?,
    val title: String,
    val message: String?,
    val negativeText: String?,
    val positiveText: String?,
) : Parcelable
