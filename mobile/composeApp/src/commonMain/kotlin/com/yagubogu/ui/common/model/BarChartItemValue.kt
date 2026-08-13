package com.yagubogu.ui.common.model

import androidx.compose.ui.graphics.Color

data class BarChartItemValue(
    val strokeColor: Color,
    val titleLabel: BarChartLabel?,
    val amount: Int,
    val dataLabel: BarChartLabel?,
)
