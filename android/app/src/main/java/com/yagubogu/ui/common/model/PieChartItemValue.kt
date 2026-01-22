package com.yagubogu.ui.common.model

import androidx.compose.ui.graphics.Color
import com.yagubogu.ui.theme.Primary500

data class PieChartItemValue(
    val strokeColor: Color = Primary500,
    val percentage: Float = 100f,
)
