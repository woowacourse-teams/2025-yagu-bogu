package com.yagubogu.ui.common.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.yagubogu.ui.common.model.BarChartItemValue
import com.yagubogu.ui.common.model.BarChartLabel.Companion.DefaultBarChartDataLabel
import com.yagubogu.ui.common.model.BarChartLabel.Companion.DefaultBarChartTitleLabel
import com.yagubogu.ui.theme.Primary500

/**
 * 여러 개의 가로 막대 그래프를 리스트 형태로 표시하는 애니메이션 차트 컴포넌트입니다.
 *
 * 각 막대의 시작점과 끝점을 자동으로 정렬하기 위해 [items]의 모든 레이블 텍스트를 측정하여
 * 가장 긴 레이블 기준으로 영역을 할당합니다. 데이터 레이블은 막대 끝을 따라 움직이며,
 * 모든 차트가 동일한 시각적 기준선을 유지합니다.
 *
 * ## 주요 기능
 * - 자동 레이블 정렬: TextMeasurer를 사용하여 타이틀과 데이터 레이블의 최대 너비를 자동 계산합니다.
 * - 시작점/끝점 통일: 글자 수가 다른 레이블이 섞여 있어도 모든 막대의 시작/끝 지점이 수직으로 일치합니다.
 * - 비율 기반 표현: 가장 큰 값을 100%로 하여 상대적 크기를 시각화합니다.
 * - 선형 애니메이션: 각 막대가 0에서 최종 값까지 부드럽게 차오릅니다.
 *
 * @param modifier 레이아웃 수정을 위한 모디파이어.
 * @param strokeVerticalGap 각 막대 그래프 사이의 수직 간격.
 * @param durationMillis 각 막대의 애니메이션 완료 시간(밀리초). 모든 막대가 동시에 시작됩니다.
 * @param strokeWidth 각 막대의 높이(두께).
 * @param items 차트에 표시할 데이터 항목 리스트. 리스트 순서대로 상단부터 배치됩니다.
 * @param strokeShape 막대의 형태를 정의하는 Shape. 기본값은 왼쪽 직각, 오른쪽 둥근 모양입니다.
 *
 * @see SingleAnimatedHorizontalBarChart 개별 막대 차트 컴포넌트
 * @see BarChartItemValue 차트 데이터 모델
 */
@Composable
fun AnimatedBarChart(
    modifier: Modifier = Modifier,
    strokeVerticalGap: Dp = 10.dp,
    durationMillis: Int = 1000,
    strokeWidth: Dp = 25.dp,
    items: List<BarChartItemValue>,
    strokeShape: RoundedCornerShape =
        RoundedCornerShape(
            topStart = 0.dp,
            bottomStart = 0.dp,
            topEnd = 100.dp,
            bottomEnd = 100.dp,
        ),
) {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current

    val maxValue =
        remember(items) {
            items.maxOfOrNull { it.amount.toFloat() } ?: 0f
        }

    val maxTitleWidth =
        remember(items) {
            items.maxOfOrNull { item ->
                val widthPx =
                    textMeasurer
                        .measure(
                            text = item.titleLabel?.value ?: "",
                            style = item.titleLabel?.textStyle ?: TextStyle.Default,
                        ).size.width
                with(density) { widthPx.toDp() }
            } ?: 60.dp
        }

    val maxDataWidth =
        remember(items) {
            items.maxOfOrNull { item ->
                val widthPx =
                    textMeasurer
                        .measure(
                            text = item.dataLabel?.value ?: "",
                            style = item.dataLabel?.textStyle ?: TextStyle.Default,
                        ).size.width
                with(density) { widthPx.toDp() }
            } ?: 80.dp
        }

    Column(
        modifier = modifier,
    ) {
        items.forEachIndexed { index, item ->
            if (index > 0) {
                Spacer(modifier = Modifier.height(strokeVerticalGap))
            }
            Row {
                SingleAnimatedHorizontalBarChart(
                    modifier = Modifier.height(strokeWidth),
                    durationMillis = durationMillis,
                    strokeColor = item.strokeColor,
                    item = item,
                    maxValue = maxValue,
                    strokeShape = strokeShape,
                    maxTitleLabelWidth = maxTitleWidth,
                    maxDataTitleWidth = maxDataWidth,
                )
            }
        }
    }
}

@Preview("AnimatedBarChart 차트")
@Composable
private fun AnimatedBarChartPreview() {
    AnimatedBarChart(
        items =
            listOf(
                BarChartItemValue(
                    strokeColor = Primary500,
                    titleLabel = DefaultBarChartTitleLabel.copy(value = "잠실"),
                    amount = 50,
                    dataLabel = DefaultBarChartDataLabel.copy(value = "50회"),
                ),
                BarChartItemValue(
                    strokeColor = Primary500,
                    titleLabel = DefaultBarChartTitleLabel.copy(value = "고척"),
                    amount = 30,
                    dataLabel = DefaultBarChartDataLabel.copy(value = "30회"),
                ),
            ),
    )
}

@Preview("AnimatedBarChart 차트 라벨 길이가 길 경우")
@Composable
private fun AnimatedBarChartPreview2() {
    AnimatedBarChart(
        items =
            listOf(
                BarChartItemValue(
                    strokeColor = Primary500,
                    titleLabel = DefaultBarChartTitleLabel.copy(value = "잠실구장"),
                    amount = 50,
                    dataLabel = DefaultBarChartDataLabel.copy(value = "50번 인증"),
                ),
                BarChartItemValue(
                    strokeColor = Primary500,
                    titleLabel = DefaultBarChartTitleLabel.copy(value = "고척스카이돔"),
                    amount = 30,
                    dataLabel = DefaultBarChartDataLabel.copy(value = "30회"),
                ),
            ),
    )
}
