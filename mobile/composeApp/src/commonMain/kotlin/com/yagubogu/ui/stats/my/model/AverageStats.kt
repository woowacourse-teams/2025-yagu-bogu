package com.yagubogu.ui.stats.my.model

import androidx.compose.runtime.Composable
import com.yagubogu.ui.stats.my.component.StatItemValue
import com.yagubogu.ui.util.formatOneDecimal
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

data class AverageStats(
    val averageRuns: Double = 0.0,
    val concededRuns: Double = 0.0,
    val averageErrors: Double = 0.0,
    val averageHits: Double = 0.0,
    val concededHits: Double = 0.0,
)

@Composable
fun AverageStats?.toStatItemValue(
    resource: StringResource,
    selector: (AverageStats) -> Double,
): StatItemValue {
    // 1. 전체 데이터가 null이면 로딩 상태 반환
    val stats = this ?: return StatItemValue.Loading

    // 2. 데이터 추출
    val rawValue = selector(stats)

    // 3. 리소스와 함께 포맷팅하여 Data 상태로 반환
    return StatItemValue.Data(
        stringResource(resource, rawValue.formatOneDecimal()),
    )
}
