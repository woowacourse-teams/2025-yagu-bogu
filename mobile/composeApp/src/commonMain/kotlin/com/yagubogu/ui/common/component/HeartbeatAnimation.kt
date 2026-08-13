package com.yagubogu.ui.common.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.random.Random

@Composable
fun HeartbeatAnimation(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    val scale = remember { Animatable(1f) }

    LaunchedEffect(enabled) {
        if (!enabled) return@LaunchedEffect

        val randomDelay: Long = Random.nextLong(0, 1_000)
        delay(randomDelay)

        while (isActive) {
            scale.apply {
                // Strong beat: 1 → 1.2 → 1
                animateTo(
                    targetValue = 1.2f,
                    animationSpec = tween(durationMillis = 150),
                )
                animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 150),
                )

                // Soft beat: 1 → 1.15 → 1
                animateTo(
                    targetValue = 1.15f,
                    animationSpec = tween(durationMillis = 150),
                )
                animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 150),
                )
            }

            // 2.2초 휴식
            delay(2_200)
        }
    }

    Box(
        modifier =
            modifier
                .graphicsLayer(
                    scaleX = scale.value,
                    scaleY = scale.value,
                ),
    ) {
        content()
    }
}
