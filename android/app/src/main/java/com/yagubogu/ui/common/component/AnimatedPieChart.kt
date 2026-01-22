package com.yagubogu.ui.common.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.yagubogu.ui.common.model.PieChartItemValue
import com.yagubogu.ui.theme.Gray400
import com.yagubogu.ui.theme.Gray900
import com.yagubogu.ui.theme.Primary500

/**
 * 여러 항목의 비율을 시각화하는 애니메이션 원형 차트 컴포저블입니다.
 *
 * 각 항목을 별도의 레이어로 간주하고, 누적된 퍼센트만큼 큰 원부터 작은 원 순서로
 * 겹쳐 그려서(Stacking) 하나의 파이 차트처럼 보이게 합니다. 모든 항목은 12시 방향에서
 * 시작하여 시계 방향으로 동시에 채워지는 애니메이션을 가집니다.
 *
 * @param durationMillis 차트가 채워지는 애니메이션의 총 재생 시간(밀리초).
 * @param strokeWidth 차트 테두리(호)의 두께.
 * @param items 차트에 표시할 각 항목의 정보(색상, 퍼센트)가 담긴 리스트.
 *              항목들의 퍼센트 합계가 100이 되도록 데이터를 전달하는 것이 권장됩니다.
 */
@Composable
fun AnimatedPieChart(
    modifier: Modifier = Modifier,
    durationMillis: Int = 1000,
    strokeWidth: Dp = 25.dp,
    items: List<PieChartItemValue>,
) {
    val cumulativePercentages =
        remember(items) {
            items
                .scan(0f) { acc, item -> acc + item.percentage }
                .drop(1)
                .reversed()
        }

    val reversedItems = remember(items) { items.reversed() }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier,
    ) {
        reversedItems.forEachIndexed { index, item ->
            SingleAnimatedPieChart(
                modifier = modifier,
                durationMillis = durationMillis,
                strokeWidth = strokeWidth,
                strokeColor = item.strokeColor,
                sweepAngle = (cumulativePercentages[index] / 100f) * 360f,
            )
        }
    }
}

@Preview("AnimatedPieChart 차트")
@Composable
private fun AnimatedPieChartPreview() {
    AnimatedPieChart(
        modifier = Modifier.size(200.dp),
        items =
            listOf(
                PieChartItemValue(strokeColor = Primary500, percentage = 50f),
                PieChartItemValue(strokeColor = Gray400, percentage = 30f),
                PieChartItemValue(strokeColor = Gray900, percentage = 20f),
            ),
    )
}
