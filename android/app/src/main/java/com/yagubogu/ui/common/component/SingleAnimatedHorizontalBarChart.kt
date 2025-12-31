package com.yagubogu.ui.common.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.yagubogu.ui.common.model.BarChartItemValue
import com.yagubogu.ui.common.model.BarChartLabel.Companion.DefaultBarChartDataLabel
import com.yagubogu.ui.common.model.BarChartLabel.Companion.DefaultBarChartTitleLabel
import com.yagubogu.ui.theme.Primary500

@Composable
fun SingleAnimatedHorizontalBarChart(
    modifier: Modifier = Modifier,
    item: BarChartItemValue,
    maxValue: Float = 100f,
    maxTitleLabelWidth: Dp = 40.dp,
    maxDataTitleWidth: Dp = 40.dp,
    durationMillis: Int = 1000,
    strokeColor: Color = Primary500,
    strokeShape: RoundedCornerShape = RoundedCornerShape(0.dp, 100.dp, 100.dp, 0.dp),
) {
    var targetProgress by remember { mutableFloatStateOf(0f) }
    val progress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = durationMillis, easing = LinearEasing),
        label = "BarProgress",
    )

    LaunchedEffect(Unit) { targetProgress = 1f }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 1. 시작 레이블 영역 (고정 너비로 시작점 일치)
        if (item.titleLabel != null) {
            Box(modifier = Modifier.width(maxTitleLabelWidth)) {
                Text(text = item.titleLabel.value, style = item.titleLabel.textStyle)
            }
            Spacer(modifier = Modifier.width(item.titleLabel.gap))
        }

        // 2. 가로 막대 영역 (weight로 가변 공간 확보)
        Box(
            modifier =
                modifier
                    .weight(1f), // 핵심: 남은 공간을 모두 차지함
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(
                            fraction =
                                (item.amount.toFloat() / maxValue).coerceIn(
                                    0f,
                                    1f,
                                ) * progress,
                        ).background(strokeColor, strokeShape),
            )
        }

        if (item.dataLabel != null) {
            Spacer(modifier = Modifier.width(item.dataLabel.gap))
            Box(
                modifier = Modifier.width(maxDataTitleWidth),
                contentAlignment = Alignment.CenterStart,
            ) {
                Text(text = item.dataLabel.value, style = item.dataLabel.textStyle)
            }
        }
    }
}

@Preview("단일 가로 바 차트")
@Composable
private fun SingleAnimatedPieChartPreview() {
    SingleAnimatedHorizontalBarChart(
        modifier = Modifier.height(18.dp),
        maxValue = 50f,
        item =
            BarChartItemValue(
                strokeColor = Primary500,
                titleLabel = DefaultBarChartTitleLabel.copy(value = "고척"),
                amount = 50,
                dataLabel = DefaultBarChartDataLabel.copy(value = "50회"),
            ),
    )
}
