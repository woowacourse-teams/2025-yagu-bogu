package com.yagubogu.ui.common.model

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yagubogu.ui.theme.PretendardMedium
import com.yagubogu.ui.theme.PretendardMedium12

data class BarChartLabel(
    val value: String,
    val gap: Dp,
    val textStyle: TextStyle,
) {
    companion object {
        val DefaultBarChartTitleLabel
            @Composable get() =
                BarChartLabel(
                    value = "Title",
                    gap = 20.dp,
                    textStyle = PretendardMedium.copy(fontSize = 14.sp),
                )
        val DefaultBarChartDataLabel
            @Composable get() =
                BarChartLabel(
                    value = "Data",
                    gap = 8.dp,
                    textStyle = PretendardMedium12,
                )
    }
}
