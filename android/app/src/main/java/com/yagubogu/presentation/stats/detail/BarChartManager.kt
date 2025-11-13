package com.yagubogu.presentation.stats.detail

import android.content.Context
import androidx.core.content.res.ResourcesCompat
import com.github.mikephil.charting.charts.HorizontalBarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.yagubogu.R

/**
 * HorizontalBarChart의 스타일과 데이터를 관리하는 클래스
 */
class BarChartManager(
    private val context: Context,
    private val chart: HorizontalBarChart,
) {
    private val pretendardTypeface =
        ResourcesCompat.getFont(context, R.font.pretendard_medium)

    /**
     * 차트 기본 스타일 설정
     */
    fun setupChart() {
        chart.apply {
            setNoDataText("")
            description.isEnabled = false
            legend.isEnabled = false
            setTouchEnabled(false)

            // 값 축 숨김
            axisLeft.apply {
                isEnabled = false
                axisMinimum = 0f
            }
            axisRight.isEnabled = false

            // X축 (실제로는 화면에서 Y축처럼 보임)
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM // 왼쪽에 라벨이 오도록
                setDrawGridLines(false)
                setDrawAxisLine(false)
                granularity = 1f // 인덱스 단위로 라벨 표시
                isGranularityEnabled = true
                textSize = TEXT_SIZE
                typeface = pretendardTypeface
                xOffset = 10f // 라벨 좌우 간격
            }

            // 오른쪽만 둥글게 그리는 Custom Renderer
            renderer =
                RoundedHorizontalBarChartRenderer(RADIUS, this, animator, viewPortHandler)
        }
    }

    /**
     * 데이터를 차트에 바인딩
     */
    fun loadData(stadiumVisitCounts: List<StadiumVisitCount>) {
        // HorizontalBarChart index 0이 아래에서부터 그려지므로 reversed 한 List 적용
        val itemsTopToBottom: List<StadiumVisitCount> = stadiumVisitCounts.reversed()

        // index = X축 값, visitCounts = Y축 값
        val entries: List<BarEntry> =
            itemsTopToBottom.mapIndexed { index, item ->
                BarEntry(index.toFloat(), item.visitCounts.toFloat())
            }

        // Bar 스타일 정의
        val dataSet =
            BarDataSet(entries, BAR_DATA_SET_LABEL).apply {
                color = context.getColor(R.color.primary500)
                valueTextSize = TEXT_SIZE
                valueTextColor = context.getColor(android.R.color.black)
                valueTypeface = pretendardTypeface
                setDrawValues(true)

                // 값 포맷 (0 → "-" 처리)
                valueFormatter =
                    object : ValueFormatter() {
                        override fun getBarLabel(e: BarEntry?): String {
                            val visitCount: Int = e?.y?.toInt() ?: 0
                            return if (visitCount == 0) {
                                "-"
                            } else {
                                context.getString(
                                    R.string.all_count,
                                    visitCount,
                                )
                            }
                        }
                    }
            }

        chart.apply {
            data = BarData(dataSet).apply { barWidth = 0.6f }

            // X축 라벨에 구장 이름 매핑
            xAxis.valueFormatter =
                IndexAxisValueFormatter(
                    itemsTopToBottom.map { it.location },
                )
            xAxis.labelCount = itemsTopToBottom.size

            animateY(BAR_CHART_ANIMATION_MILLISECOND)
            invalidate()
        }
    }

    companion object {
        private const val BAR_DATA_SET_LABEL = "구장 방문 횟수"
        private const val BAR_CHART_ANIMATION_MILLISECOND = 700

        private const val TEXT_SIZE = 14f
        private const val RADIUS = 40f
    }
}
