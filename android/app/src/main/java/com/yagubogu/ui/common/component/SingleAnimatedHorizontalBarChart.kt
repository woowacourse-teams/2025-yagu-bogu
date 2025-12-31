package com.yagubogu.ui.common.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.yagubogu.ui.theme.Primary500

/**
 * 단일 가로 막대 그래프 애니메이션을 그리는 컴포저블입니다.
 *
 * 지정된 [maxValue] 대비 [value]의 비율만큼 막대가 왼쪽에서 오른쪽으로 차오르는 애니메이션을 제공합니다.
 * 왼쪽은 직각, 오른쪽은 둥근 형태가 기본값이며 [strokeShape]를 통해 커스텀 가능합니다.
 *
 * @param modifier 레이아웃 수정을 위한 모디파이어. 높이(height)를 지정하지 않으면 내용물에 맞게 결정됩니다.
 * @param durationMillis 애니메이션이 완료되기까지의 시간(밀리초). 기본값은 1000ms입니다.
 * @param strokeColor 막대의 색상.
 * @param value 현재 값.
 * @param maxValue 최대 값. [value] / [maxValue] 비율로 너비가 결정됩니다.
 * @param strokeShape 막대의 모양. 기본값은 오른쪽만 둥근 형태입니다.
 */
@Composable
fun SingleAnimatedHorizontalBarChart(
    modifier: Modifier = Modifier,
    durationMillis: Int = 1000,
    strokeColor: Color = Primary500,
    value: Float = 30f,
    maxValue: Float = 100f,
    strokeShape: RoundedCornerShape =
        RoundedCornerShape(
            topStart = 0.dp,
            bottomStart = 0.dp,
            topEnd = 100.dp,
            bottomEnd = 100.dp,
        ),
) {
    var targetProgress by remember { mutableFloatStateOf(0f) }
    val progress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = durationMillis, easing = LinearEasing),
    )

    LaunchedEffect(Unit) {
        targetProgress = 1f
    }

    Box(
        modifier =
            modifier
                .fillMaxWidth(),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = (value / maxValue).coerceIn(0f, 1f) * progress)
                    .background(strokeColor, strokeShape),
        )
    }
}

@Preview("단일 가로 바 차트")
@Composable
private fun SingleAnimatedPieChartPreview() {
    SingleAnimatedHorizontalBarChart(
        modifier = Modifier.height(25.dp),
    )
}
