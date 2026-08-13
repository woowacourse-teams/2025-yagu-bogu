package com.yagubogu.ui.stats.my.model

import com.yagubogu.ui.stats.my.component.StatItemValue

data class StatsMyUiModel(
    val winCount: Int = 0,
    val drawCount: Int = 0,
    val loseCount: Int = 0,
    val totalCount: Int = 0,
    val winningPercentage: Float = 0f,
    val myTeam: String? = null,
    val luckyStadium: String? = null,
) {
    val etcPercentage: Float get() = FULL_PERCENTAGE - winningPercentage

    companion object {
        private const val FULL_PERCENTAGE = 100f
    }
}

fun <T> StatsMyUiModel?.toStatItemValue(propertySelector: (StatsMyUiModel) -> T?): StatItemValue {
    // 1. 전체 모델이 null이면 로딩
    val model = this ?: return StatItemValue.Loading

    // 2. 선택된 프로퍼티 값 가져오기
    val value = propertySelector(model)

    // 3. 값이 null이면 NoData, 아니면 String으로 변환하여 반환
    return if (value != null) {
        StatItemValue.Data(value.toString())
    } else {
        StatItemValue.NoData
    }
}
