package com.yagubogu.ui.util

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier =
    composed {
        clickable(
            indication = null,
            interactionSource = remember { MutableInteractionSource() },
        ) {
            onClick()
        }
    }

fun Modifier.shimmerLoading(): Modifier =
    composed {
        // 무한 반복 애니메이션 정의
        val transition = rememberInfiniteTransition(label = "shimmerTransition")
        val translateAnim =
            transition.animateFloat(
                initialValue = 0f,
                targetValue = 1000f, // 이동 범위 (size에 맞게 충분히 크게 설정)
                animationSpec =
                    infiniteRepeatable(
                        animation =
                            tween(
                                durationMillis = 1200,
                                easing = LinearEasing,
                            ),
                        repeatMode = RepeatMode.Restart,
                    ),
                label = "shimmerAnim",
            )

        // Shimmer 색상 (회색 → 밝은 회색 → 회색)
        val shimmerColors =
            listOf(
                Color.LightGray.copy(alpha = 0.6f),
                Color.LightGray.copy(alpha = 0.2f),
                Color.LightGray.copy(alpha = 0.6f),
            )

        drawWithContent {
            // Shimmer 효과 브러시
            val brush =
                Brush.linearGradient(
                    colors = shimmerColors,
                    start = Offset(translateAnim.value - size.width, 0f),
                    end = Offset(translateAnim.value, size.height),
                )
            drawRect(brush = brush)
        }
    }
