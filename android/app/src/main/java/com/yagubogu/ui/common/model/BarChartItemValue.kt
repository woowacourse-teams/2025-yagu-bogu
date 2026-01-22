package com.yagubogu.ui.common.model

import androidx.compose.ui.graphics.Color
import com.yagubogu.ui.theme.Primary500

data class BarChartItemValue(
    val strokeColor: Color = Primary500,
    val titleLabel: BarChartLabel? = null,
    val amount: Int = 0,
    val dataLabel: BarChartLabel? = null,
)
