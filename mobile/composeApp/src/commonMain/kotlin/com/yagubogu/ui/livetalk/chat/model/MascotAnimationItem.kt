package com.yagubogu.ui.livetalk.chat.model

import androidx.compose.ui.geometry.Offset
import org.jetbrains.compose.resources.DrawableResource

data class MascotAnimationItem(
    val id: Long,
    val mascot: DrawableResource,
    val startOffset: Offset,
)
