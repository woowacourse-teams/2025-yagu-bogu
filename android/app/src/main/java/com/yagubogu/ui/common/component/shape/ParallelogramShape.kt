package com.yagubogu.ui.common.component.shape

import androidx.compose.foundation.shape.CornerSize
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

/**
 * Parallelogram (평행사변형) drawn with Jetpack Compose
 *
 * https://github.com/uuranus/compose-shapes
 */
class ParallelogramShape(
    private val skewed: Float = 0.2f,
    private val cornerStyle: CornerStyle,
    private val topStart: CornerSize,
    private val topEnd: CornerSize,
    private val bottomEnd: CornerSize,
    private val bottomStart: CornerSize,
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val path =
            when (cornerStyle) {
                CornerStyle.ROUNDED -> {
                    drawRoundedParallelogramShape(
                        size,
                        skewed = skewed,
                        topStart = topStart.toPx(size, density),
                        topEnd = topEnd.toPx(size, density),
                        bottomStart = bottomStart.toPx(size, density),
                        bottomEnd = bottomEnd.toPx(size, density),
                    )
                }

                CornerStyle.INNER_ROUNDED -> {
                    drawInnerRoundedParallelogramShape(
                        size,
                        skewed = skewed,
                        topStart = topStart.toPx(size, density),
                        topEnd = topEnd.toPx(size, density),
                        bottomStart = bottomStart.toPx(size, density),
                        bottomEnd = bottomEnd.toPx(size, density),
                    )
                }

                CornerStyle.CUT -> {
                    drawCutParallelogramShape(
                        size,
                        skewed = skewed,
                        topStart = topStart.toPx(size, density),
                        topEnd = topEnd.toPx(size, density),
                        bottomStart = bottomStart.toPx(size, density),
                        bottomEnd = bottomEnd.toPx(size, density),
                    )
                }
            }
        return Outline.Generic(path)
    }
}

private fun drawRoundedParallelogramShape(
    size: Size,
    skewed: Float,
    topStart: Float,
    topEnd: Float,
    bottomEnd: Float,
    bottomStart: Float,
): Path {
    val path = Path()

    val width = size.width
    val height = size.height
    val skewedWidth = skewed * width
    val y1 = 0f
    val x2 = width - skewedWidth
    val topRightAngle = -atan((height - y1) / (x2 - width))
    val topRightAngleDegree = (topRightAngle * 180 / Math.PI)
    val halfTan = tan((topRightAngle / 2))

    path.apply {
        arcTo(
            rect =
                Rect(
                    offset =
                        Offset(
                            width - topEnd /
                                halfTan - topEnd,
                            0f,
                        ),
                    size = Size(topEnd * 2, topEnd * 2),
                ),
            startAngleDegrees = 270f,
            sweepAngleDegrees = 180f - topRightAngleDegree.toFloat(),
            forceMoveTo = false,
        )

        arcTo(
            rect =
                Rect(
                    offset =
                        Offset(
                            width - skewedWidth - bottomEnd * halfTan - bottomEnd,
                            height - bottomEnd * 2,
                        ),
                    size =
                        Size(
                            bottomEnd * 2,
                            bottomEnd * 2,
                        ),
                ),
            startAngleDegrees = (90f - topRightAngleDegree).toFloat(),
            sweepAngleDegrees = topRightAngleDegree.toFloat(),
            forceMoveTo = false,
        )

        arcTo(
            rect =
                Rect(
                    offset =
                        Offset(
                            bottomStart / halfTan - bottomStart,
                            height - bottomStart * 2,
                        ),
                    size =
                        Size(
                            bottomStart * 2,
                            bottomStart * 2,
                        ),
                ),
            startAngleDegrees = 90f,
            sweepAngleDegrees = 180f - topRightAngleDegree.toFloat(),
            forceMoveTo = false,
        )

        arcTo(
            rect =
                Rect(
                    offset =
                        Offset(
                            skewedWidth + topStart * halfTan - topStart,
                            0f,
                        ),
                    size = Size(topStart * 2, topStart * 2),
                ),
            startAngleDegrees = (270f - topRightAngleDegree).toFloat(),
            sweepAngleDegrees = topRightAngleDegree.toFloat(),
            forceMoveTo = false,
        )
        close()
    }

    return path
}

private fun drawInnerRoundedParallelogramShape(
    size: Size,
    skewed: Float,
    topStart: Float,
    topEnd: Float,
    bottomEnd: Float,
    bottomStart: Float,
): Path {
    val path = Path()

    val width = size.width
    val height = size.height
    val skewedWidth = skewed * width
    val y1 = 0f
    val x2 = width - skewedWidth
    val topRightAngle = -atan((height - y1) / (x2 - width))
    val topRightAngleDegree = (topRightAngle * 180 / Math.PI)

    path.apply {
        arcTo(
            rect =
                Rect(
                    offset =
                        Offset(
                            width - topEnd,
                            -topEnd,
                        ),
                    size = Size(topEnd * 2, topEnd * 2),
                ),
            startAngleDegrees = 180f,
            sweepAngleDegrees = -topRightAngleDegree.toFloat(),
            forceMoveTo = false,
        )

        arcTo(
            rect =
                Rect(
                    offset =
                        Offset(
                            width - skewedWidth - bottomEnd,
                            height - bottomEnd,
                        ),
                    size =
                        Size(
                            bottomEnd * 2,
                            bottomEnd * 2,
                        ),
                ),
            startAngleDegrees = (-topRightAngleDegree).toFloat(),
            sweepAngleDegrees = -180f + topRightAngleDegree.toFloat(),
            forceMoveTo = false,
        )

        arcTo(
            rect =
                Rect(
                    offset =
                        Offset(
                            -bottomStart,
                            height - bottomStart,
                        ),
                    size =
                        Size(
                            bottomStart * 2,
                            bottomStart * 2,
                        ),
                ),
            startAngleDegrees = 0f,
            sweepAngleDegrees = -topRightAngleDegree.toFloat(),
            forceMoveTo = false,
        )

        arcTo(
            rect =
                Rect(
                    offset =
                        Offset(
                            skewedWidth - topStart,
                            -topStart,
                        ),
                    size = Size(topStart * 2, topStart * 2),
                ),
            startAngleDegrees = 180f - topRightAngleDegree.toFloat(),
            sweepAngleDegrees = (topRightAngleDegree - 180f).toFloat(),
            forceMoveTo = false,
        )

        close()
    }

    return path
}

private fun drawCutParallelogramShape(
    size: Size,
    skewed: Float,
    topStart: Float,
    topEnd: Float,
    bottomEnd: Float,
    bottomStart: Float,
): Path {
    val path = Path()

    val width = size.width
    val height = size.height
    val skewedWidth = skewed * width
    val y1 = 0f
    val x2 = width - skewedWidth
    val topRightAngle = -atan((height - y1) / (x2 - width))

    path.apply {
        moveTo(width - topEnd, 0f)

        lineTo(width - topEnd * cos(topRightAngle), topEnd * sin(topRightAngle))

        lineTo(
            width - skewedWidth + bottomEnd * cos(topRightAngle),
            height - bottomEnd * sin(topRightAngle),
        )

        lineTo(width - skewedWidth - bottomEnd, height)

        lineTo(bottomStart, height)

        lineTo(bottomStart * cos(topRightAngle), height - bottomStart * sin(topRightAngle))

        lineTo(skewedWidth - topStart * cos(topRightAngle), topStart * sin(topRightAngle))

        lineTo(skewedWidth + topStart, 0f)

        close()
    }

    return path
}

fun ParallelogramShape(
    skewed: Float,
    cornerStyle: CornerStyle = CornerStyle.ROUNDED,
    cornerSize: CornerSize = CornerSize(0.dp),
) = ParallelogramShape(skewed, cornerStyle, cornerSize, cornerSize, cornerSize, cornerSize)

fun ParallelogramShape(
    skewed: Float,
    cornerStyle: CornerStyle = CornerStyle.ROUNDED,
    size: Dp = 0.dp,
) = ParallelogramShape(skewed, cornerStyle, CornerSize(size))

fun ParallelogramShape(
    skewed: Float,
    cornerStyle: CornerStyle = CornerStyle.ROUNDED,
    topStart: Dp = 0.dp,
    topEnd: Dp = 0.dp,
    bottomEnd: Dp = 0.dp,
    bottomStart: Dp = 0.dp,
) = ParallelogramShape(
    skewed = skewed,
    cornerStyle = cornerStyle,
    topStart = CornerSize(topStart),
    topEnd = CornerSize(topEnd),
    bottomEnd = CornerSize(bottomEnd),
    bottomStart = CornerSize(bottomStart),
)
