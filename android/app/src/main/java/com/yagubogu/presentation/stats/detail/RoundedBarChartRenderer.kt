package com.yagubogu.presentation.stats.detail

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import com.github.mikephil.charting.animation.ChartAnimator
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.renderer.BarChartRenderer
import com.github.mikephil.charting.utils.ViewPortHandler

/**
 * MPAndroidChart의 BarChartRenderer를 확장하여
 * - 막대 상단 모서리를 둥글게 처리
 * - 값 라벨을 커스텀 방식으로 표시
 * 하는 전용 Renderer
 */
class RoundedBarChartRenderer(
    chart: BarChart,
    viewPortHandler: ViewPortHandler,
    private val animator: ChartAnimator,
    private val radius: Float, // Bar 상단 둥근 모서리 반경
) : BarChartRenderer(chart, animator, viewPortHandler) {
    // 값 라벨을 직접 그리기 위한 Paint 객체
    private val valuePaint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = Paint.Align.CENTER
        }

    override fun drawDataSet(
        c: Canvas,
        dataSet: IBarDataSet,
        index: Int,
    ) {
        val trans = mChart.getTransformer(dataSet.axisDependency)
        val barCount = dataSet.entryCount
        val barWidth = mChart.barData.barWidth

        for (i in 0 until barCount) {
            val e = dataSet.getEntryForIndex(i)
            if (e is BarEntry) {
                val x = e.x
                val y = e.y * animator.phaseY

                // Bar 좌표 계산 (x 중심 기준)
                val left = x - barWidth / 2f
                val right = x + barWidth / 2f
                val rect = RectF(left, 0f, right, y)
                // 데이터 값 → 픽셀 좌표 변환
                trans.rectValueToPixel(rect)

                // 상단만 둥근 Path 생성
                val path = Path()
                val radii =
                    floatArrayOf(
                        radius,
                        radius, // Top-left
                        radius,
                        radius, // Top-right
                        0f,
                        0f, // Bottom-right
                        0f,
                        0f, // Bottom-left
                    )
                path.addRoundRect(rect, radii, Path.Direction.CW)

                // Bar 색상 적용 후 그리기
                mRenderPaint.color = dataSet.getColor(i)
                c.drawPath(path, mRenderPaint)

                // 값 라벨 그리기 (바 내부 하단 고정)
                if (dataSet.isDrawValuesEnabled) {
                    valuePaint.color = if (e.y.toInt() == 0) Color.BLACK else Color.WHITE
                    valuePaint.textSize = dataSet.valueTextSize
                    dataSet.valueTypeface?.let { valuePaint.typeface = it }

                    val valueText = dataSet.valueFormatter.getBarLabel(e)

                    // Paint ascent/descent 사용해서 바 내부 하단 고정
                    val textHeight = valuePaint.descent() - valuePaint.ascent()
                    // 하단에서 약간 위로 offset
                    val labelY = rect.bottom - textHeight / 4f

                    c.drawText(valueText, rect.centerX(), labelY, valuePaint)
                }
            }
        }
    }
}
