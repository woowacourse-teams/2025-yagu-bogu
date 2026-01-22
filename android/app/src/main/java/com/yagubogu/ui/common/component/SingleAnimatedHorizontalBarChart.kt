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

/**
 * 데이터 레이블이 막대 끝을 따라 움직이는 단일 가로 막대 그래프 컴포저블입니다.
 *
 * [item]의 [amount] 값을 [maxValue] 대비 비율로 계산하여 막대를 그리며,
 * 데이터 레이블이 애니메이션 진행 중에도 항상 막대의 우측 끝에 위치하도록 설계되었습니다.
 * [weight] Modifier를 활용하여 막대, 레이블, 여백의 비율을 동적으로 조절합니다.
 *
 * ## 레이아웃 구조
 * ```
 * [타이틀 고정영역] [막대(가변)] [데이터레이블 고정] [여백(가변)]
 * ```
 *
 * ## 정렬 보장
 * 여러 개의 차트를 세로로 쌓을 때, 모든 인스턴스에 동일한 [maxTitleLabelWidth]와
 * [maxDataTitleWidth] 값을 전달하면 막대의 시작점과 끝점이 수직으로 일치합니다.
 * (일반적으로 [AnimatedBarChart]에서 자동 계산된 최대 너비를 받아 사용합니다)
 *
 * ## 애니메이션 동작
 * - 컴포저블이 화면에 나타날 때 자동으로 시작되며 데이터 레이블과 함꼐 선형으로 차오릅니다.
 *
 * @param modifier 레이아웃 수정을 위한 모디파이어, [Modifier.height]가 막대의 높이입니다.
 * @param item 차트에 표시할 데이터 항목. 타이틀 레이블, 수치, 데이터 레이블을 포함합니다.
 * @param maxValue 비율 계산의 기준이 되는 최대값. [item.amount] / [maxValue]로 막대 길이를 결정합니다.
 * @param maxTitleLabelWidth 타이틀 레이블 영역의 고정 너비. 여러 차트의 시작점을 맞추기 위해 사용됩니다.
 * @param maxDataTitleWidth 데이터 레이블 영역의 고정 너비. 여러 차트의 끝점을 맞추기 위해 사용됩니다.
 * @param durationMillis 애니메이션 완료까지의 시간(밀리초).
 * @param strokeColor 막대의 색상.
 * @param strokeShape 막대의 형태. 기본값은 왼쪽 직각, 오른쪽 둥근 모양입니다.
 *
 * @see AnimatedBarChart 여러 개의 막대를 리스트로 표시하는 부모 컴포넌트
 * @see BarChartItemValue 차트 데이터 모델
 */
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
