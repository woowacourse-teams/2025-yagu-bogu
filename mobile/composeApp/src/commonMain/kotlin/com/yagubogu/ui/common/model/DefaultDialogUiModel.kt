package com.yagubogu.ui.common.model

import kotlinx.serialization.Serializable

@Serializable
data class DefaultDialogUiModel(
    val title: String,
    val emoji: String? = null,
    val message: String? = null,
    val textAlign: String? = "Center",
    val negativeText: String? = null,
    val positiveText: String? = null,
)
