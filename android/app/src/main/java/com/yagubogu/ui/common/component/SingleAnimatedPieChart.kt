package com.yagubogu.ui.common.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.yagubogu.ui.theme.Primary500

/**
 * 단일 원형 파이 차트 애니메이션을 그리는 컴포저블입니다.
 *
 * 캔버스 위에 호(arc)를 그려, 지정한 시작 각도부터 지정한 각도만큼
 * 선이 시계 방향으로 채워지는 효과를 제공합니다. (0f~360f, 기준점은 3시방향)
 *
 * @param durationMillis 애니메이션이 완료되기까지의 시간(밀리초).
 * @param strokeColor 호(arc)의 색상.
 * @param startAngle 호가 시작되는 각도(도 단위). 기본값은 12시 방향에 해당하는 -90도입니다.
 * @param sweepAngle 애니메이션이 완료되었을 때 그려질 전체 각도(도 단위).
 * @param strokeWidth 호의 두께.
 */
@Composable
fun SingleAnimatedPieChart(
    modifier: Modifier = Modifier,
    durationMillis: Int = 1000,
    strokeColor: Color = Primary500,
    startAngle: Float = -90f,
    sweepAngle: Float = 360f,
    strokeWidth: Dp = 25.dp,
) {
    var targetProgress by remember { mutableFloatStateOf(0f) }
    val progress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = durationMillis, easing = LinearEasing),
    )

    LaunchedEffect(Unit) { targetProgress = 1f }

    Canvas(modifier = modifier) {
        val strokeWidthPx = strokeWidth.toPx()
        val halfStroke = strokeWidthPx / 2

        inset(horizontal = halfStroke, vertical = halfStroke) {
            drawArc(
                color = strokeColor,
                startAngle = startAngle,
                sweepAngle = sweepAngle * progress,
                useCenter = false,
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Butt),
            )
        }
    }
}

@Preview("단일 파이 차트")
@Composable
private fun SingleAnimatedPieChartPreview() {
    SingleAnimatedPieChart(modifier = Modifier.size(200.dp))
}
