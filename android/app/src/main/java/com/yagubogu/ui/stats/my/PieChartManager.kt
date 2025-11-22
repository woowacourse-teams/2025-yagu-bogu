package com.yagubogu.ui.stats.my

import android.content.Context
import android.graphics.Color
import androidx.core.content.res.ResourcesCompat
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.yagubogu.R

class PieChartManager(
    private val context: Context,
    private val chart: PieChart,
) {
    private val pretendardTypeface =
        ResourcesCompat.getFont(context, R.font.pretendard_medium)

    /**
     * 차트 기본 스타일 설정
     */
    fun setupChart() {
        chart.apply {
            setNoDataText("")
            legend.isEnabled = false

            isDrawHoleEnabled = true
            setHoleColor(Color.TRANSPARENT)
            holeRadius = PIE_CHART_INSIDE_HOLE_RADIUS

            description.isEnabled = false
            setDrawEntryLabels(false)
            setDrawCenterText(false)

            isRotationEnabled = false
            setTouchEnabled(false)
            animateY(PIE_CHART_ANIMATION_MILLISECOND)
        }
    }

    /**
     * 데이터를 차트에 바인딩
     */
    fun loadData(
        winningPercentage: Int,
        etcPercentage: Int,
    ) {
        val pieEntries: List<PieEntry> =
            listOf(
                PieEntry(
                    winningPercentage.toFloat(),
                    PIE_ENTRY_LABEL_WIN,
                ),
                PieEntry(
                    etcPercentage.toFloat(),
                    PIE_ENTRY_LABEL_ETC,
                ),
            )

        val myStatsChartDataSet: PieDataSet =
            PieDataSet(pieEntries, PIE_DATA_SET_LABEL).apply {
                colors =
                    listOf(
                        context.getColor(R.color.primary500),
                        context.getColor(R.color.gray300),
                    )
            }

        val pieData = PieData(myStatsChartDataSet)
        pieData.setDrawValues(false)
        chart.data = pieData
        chart.animateY(PIE_CHART_ANIMATION_MILLISECOND)
    }

    companion object {
        private const val PIE_DATA_SET_LABEL = "내 직관 승률"
        private const val PIE_ENTRY_LABEL_WIN = "Win"
        private const val PIE_ENTRY_LABEL_ETC = "Etc"
        private const val PIE_CHART_INSIDE_HOLE_RADIUS = 75f
        private const val PIE_CHART_ANIMATION_MILLISECOND = 1000
    }
}