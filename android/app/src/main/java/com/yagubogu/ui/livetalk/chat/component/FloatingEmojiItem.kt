package com.yagubogu.ui.livetalk.chat.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutQuart
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.random.Random

@Composable
fun FloatingEmojiItem(
    emoji: String,
    startOffset: Offset,
    modifier: Modifier = Modifier,
    onAnimationFinished: () -> Unit,
) {
    var isVisible by remember { mutableStateOf(true) }
    val alphaAnim = remember { Animatable(1f) }
    val offsetYAnim = remember { Animatable(0f) }
    val offsetXAnim = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch {
            // 300dp만큼 위로 이동 애니메이션
            offsetYAnim.animateTo(
                targetValue = -300f,
                animationSpec = tween(durationMillis = 1500, easing = EaseOutQuart),
            )
        }
        launch {
            // 좌우 랜덤 흔들림 애니메이션
            val randomX = (Random.nextFloat() - 0.5f) * 150f
            offsetXAnim.animateTo(
                targetValue = randomX,
                animationSpec = tween(durationMillis = 1500),
            )
        }
        launch {
            // 서서히 사라지는 애니메이션
            delay(300)
            alphaAnim.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 1200),
            )
            isVisible = false
            onAnimationFinished()
        }
    }

    val density = LocalDensity.current
    val halfBoxSize =
        remember(density) {
            with(density) { 20.dp.toPx() }
        }

    if (isVisible) {
        Box(
            modifier =
                modifier
                    .size(40.dp)
                    .offset {
                        IntOffset(
                            x = (startOffset.x - halfBoxSize + offsetXAnim.value).roundToInt(),
                            y = (startOffset.y - halfBoxSize + offsetYAnim.value).roundToInt(),
                        )
                    }.alpha(alphaAnim.value),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = emoji, fontSize = 28.sp)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FloatingEmojiItemPreview() {
    val emojiQueue = remember { mutableStateListOf<Pair<Long, Offset>>() }

    Box(
        modifier =
            Modifier
                .fillMaxSize(),
    ) {
        Box(
            modifier =
                Modifier
                    .align(Alignment.Center)
                    .size(80.dp)
                    .clickable {
                        val centerPos = Offset(540f, 1000f)
                        emojiQueue.add(System.nanoTime() to centerPos)
                    },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "⚾",
                fontSize = 40.sp,
            )
        }
        Box(modifier = Modifier.fillMaxSize()) {
            emojiQueue.forEach { (key, startPos) ->
                key(key) {
                    FloatingEmojiItem(
                        emoji = "⚾",
                        startOffset = startPos,
                        onAnimationFinished = {
                            emojiQueue.removeAll { it.first == key }
                        },
                    )
                }
            }
        }
    }
}
