package com.yagubogu.ui.common.component.shape

data class Point(
    val x: Float,
    val y: Float,
)

enum class CornerStyle {
    ROUNDED,
    INNER_ROUNDED,
    CUT,
}
