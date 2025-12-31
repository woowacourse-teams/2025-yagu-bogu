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
    val currentFraction = (item.amount.toFloat() / maxValue).coerceIn(0f, 1f) * progress

    LaunchedEffect(Unit) { targetProgress = 1f }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 타이틀 레이블 영역
        if (item.titleLabel != null) {
            Box(modifier = Modifier.width(maxTitleLabelWidth)) {
                Text(text = item.titleLabel.value, style = item.titleLabel.textStyle)
            }
            Spacer(modifier = Modifier.width(item.titleLabel.gap))
        }

        // 차트 트랙 영역 (막대 + 데이터 레이블이 움직이는 공간)
        Row(
            modifier = modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // 막대 부분 (현재 비율만큼 공간 점유)
            Box(
                modifier =
                    Modifier
                        .weight(currentFraction + 0.0001f) // 0 방어
                        .fillMaxHeight()
                        .background(strokeColor, strokeShape),
            )

            // 막대 끝의 고정 크기 데이터 레이블
            if (item.dataLabel != null) {
                Spacer(modifier = Modifier.width(item.dataLabel.gap))
                Box(
                    modifier = Modifier.width(maxDataTitleWidth),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Text(
                        text = item.dataLabel.value,
                        style = item.dataLabel.textStyle,
                        maxLines = 1,
                    )
                }
            }

            // 데이터 레이블을 그리고 남은 오른쪽 영역
            Spacer(modifier = Modifier.weight(1.0001f - currentFraction))
        }
    }
}

@Preview("단일 가로 바 차트 0%")
@Composable
private fun SingleAnimatedPieChartPreview0() {
    SingleAnimatedHorizontalBarChart(
        modifier = Modifier.height(18.dp),
        maxValue = 100f,
        item =
            BarChartItemValue(
                strokeColor = Primary500,
                titleLabel = DefaultBarChartTitleLabel.copy(value = "고척"),
                amount = 0,
                dataLabel = DefaultBarChartDataLabel.copy(value = "0회"),
            ),
    )
}


@Preview("단일 가로 바 차트 30%")
@Composable
private fun SingleAnimatedPieChartPreview30() {
    SingleAnimatedHorizontalBarChart(
        modifier = Modifier.height(18.dp),
        maxValue = 100f,
        item =
            BarChartItemValue(
                strokeColor = Primary500,
                titleLabel = DefaultBarChartTitleLabel.copy(value = "고척"),
                amount = 30,
                dataLabel = DefaultBarChartDataLabel.copy(value = "30회"),
            ),
    )
}


@Preview("단일 가로 바 차트 100%")
@Composable
private fun SingleAnimatedPieChartPreview() {
    SingleAnimatedHorizontalBarChart(
        modifier = Modifier.height(18.dp),
        maxValue = 100f,
        item =
            BarChartItemValue(
                strokeColor = Primary500,
                titleLabel = DefaultBarChartTitleLabel.copy(value = "고척"),
                amount = 100,
                dataLabel = DefaultBarChartDataLabel.copy(value = "100회"),
            ),
    )
}
