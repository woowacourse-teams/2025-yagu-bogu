package com.yagubogu.ui.common.model

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yagubogu.ui.theme.PretendardMedium
import com.yagubogu.ui.theme.PretendardMedium12

data class BarChartLabel(
    val value: String = "타이틀",
    val gap: Dp = 10.dp,
    val textStyle: TextStyle = PretendardMedium.copy(fontSize = 14.sp),
) {
    companion object {
        val DefaultBarChartTitleLabel =
            BarChartLabel(
                value = "잠실",
                gap = 20.dp,
            )
        val DefaultBarChartDataLabel =
            BarChartLabel(
                value = "100회",
                gap = 8.dp,
                textStyle = PretendardMedium12,
            )
    }
}
