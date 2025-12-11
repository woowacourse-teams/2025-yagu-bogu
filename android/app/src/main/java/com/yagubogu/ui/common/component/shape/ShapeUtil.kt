package com.yagubogu.ui.common.component.shape

import kotlin.math.PI

data class Point(
    val x: Float,
    val y: Float,
)

enum class CornerStyle {
    ROUNDED,
    INNER_ROUNDED,
    CUT,
}

internal fun Double.toRadian(): Double = (this * PI / 180.0)

internal fun Double.toDegree(): Double = (this * 180.0 / PI)

internal fun Float.toRadian(): Float = (this * PI / 180.0).toFloat()

internal fun Float.toDegree(): Float = (this * 180.0 / PI).toFloat()
