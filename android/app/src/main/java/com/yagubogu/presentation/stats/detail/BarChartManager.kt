package com.yagubogu.presentation.stats.detail

import android.content.Context
import androidx.core.content.res.ResourcesCompat
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.yagubogu.R

/**
 * BarChart 설정과 데이터 바인딩을 담당하는 헬퍼 클래스
 * Fragment는 뷰 바인딩과 뷰모델 처리만 담당하도록 책임을 분리하기 위함.
 */
class BarChartManager(
    private val context: Context,
    private val barChart: BarChart,
) {
    private val pretendardTypeface =
        ResourcesCompat.getFont(context, R.font.pretendard_medium)

    /**
     * 차트의 기본 UI와 스타일을 설정
     */
    fun setupChart() {
        barChart.apply {
            description.isEnabled = false
            legend.isEnabled = false
            setTouchEnabled(false)

            // X축 설정
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM // X축을 하단에 표시
                setDrawGridLines(false) // 격자선 제거
                setDrawAxisLine(false) // X축 라인 제거
                granularity = 1f // 최소 간격 = 1
                isGranularityEnabled = true // 간격 단위 강제
                textSize = TEXT_SIZE // 라벨 크기
                yOffset = -20f // 라벨 위치를 위로 올림 (바와의 간격 조절)
                typeface = pretendardTypeface // 지정한 폰트 적용
            }

            // Y축 제거 (값만 Bar 위에 표시하고 축은 숨김)
            axisLeft.isEnabled = false
            axisRight.isEnabled = false

            // X축 라벨이 잘리지 않도록 하단 마진 확보
            val labelHeight = xAxis.textSize * 2
            setViewPortOffsets(0f, 0f, 0f, labelHeight)

            // Bar 크기 자동 조정
            setFitBars(true)

            // Bar를 둥글게 그리기 위해 커스텀 렌더러 적용
            renderer = RoundedBarChartRenderer(this, viewPortHandler, animator, 32f)
        }
    }

    /**
     * 차트에 데이터를 로드하고 애니메이션 적용
     */
    fun loadData(stadiumVisitCounts: List<StadiumVisitCount>) {
        // Bar에 들어갈 데이터 엔트리 생성
        val barEntries =
            stadiumVisitCounts.mapIndexed { index, stadiumVisitCount ->
                BarEntry(index.toFloat(), stadiumVisitCount.visitCounts.toFloat())
            }

        // 데이터셋 정의 (Bar 스타일 및 값 포맷 지정)
        val barDataSet =
            BarDataSet(barEntries, BAR_DATA_SET_LABEL).apply {
                color = context.getColor(R.color.primary500) // Bar 색상
                valueTextSize = TEXT_SIZE // 값 텍스트 크기
                valueTextColor = context.getColor(android.R.color.black) // 값 텍스트 색상
                valueTypeface = pretendardTypeface // 값 텍스트 폰트
                setDrawValues(true) // 값 표시 여부

                // 값 포맷 정의 (0일 경우 "-"로 표시)
                valueFormatter =
                    object : ValueFormatter() {
                        override fun getBarLabel(barEntry: BarEntry?): String {
                            val value = barEntry?.y?.toInt() ?: 0
                            return if (value == 0) "-" else value.toString()
                        }
                    }
            }

        barChart.apply {
            data = BarData(barDataSet)

            // 막대 폭 비율 설정
            data.barWidth = 0.8f

            // X축 라벨을 구장 이름으로 설정
            xAxis.valueFormatter =
                IndexAxisValueFormatter(stadiumVisitCounts.map { it.stadiumName })
            xAxis.labelCount = stadiumVisitCounts.size

            // Y축 방향 애니메이션
            animateY(BAR_CHART_ANIMATION_MILLISECOND)

            // UI 업데이트
            invalidate()
        }
    }

    companion object {
        private const val BAR_DATA_SET_LABEL = "구장 방문 횟수"
        private const val BAR_CHART_ANIMATION_MILLISECOND = 700

        private const val TEXT_SIZE = 14f
    }
}
