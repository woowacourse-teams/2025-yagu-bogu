package com.yagubogu.ui.stats.detail

import android.graphics.Canvas
import android.graphics.Path
import android.graphics.RectF
import com.github.mikephil.charting.animation.ChartAnimator
import com.github.mikephil.charting.charts.HorizontalBarChart
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.renderer.HorizontalBarChartRenderer
import com.github.mikephil.charting.utils.ViewPortHandler

/**
 * HorizontalBarChart 전용 Renderer
 * - 막대 오른쪽 모서리를 둥글게 처리
 */
class RoundedHorizontalBarChartRenderer(
    private val radius: Float,
    chart: HorizontalBarChart,
    animator: ChartAnimator,
    viewPortHandler: ViewPortHandler,
) : HorizontalBarChartRenderer(chart, animator, viewPortHandler) {
    override fun drawDataSet(
        c: Canvas,
        dataSet: IBarDataSet,
        index: Int,
    ) {
        // 데이터셋에 맞는 좌표 변환기 가져오기
        val trans = mChart.getTransformer(dataSet.axisDependency)

        // MPAndroidChart 내부에서 Bar 좌표를 계산할 때 사용하는 버퍼
        // 각 막대(bar)의 좌표값을 [left, top, right, bottom] 순서로 저장
        val buffer = mBarBuffers[index]

        // 현재 애니메이션 진행률에 맞게 좌표값을 보정
        // phaseX: X축 애니메이션 진행률
        // phaseY: Y축 애니메이션 진행률
        buffer.setPhases(mAnimator.phaseX, mAnimator.phaseY)

        // 어느 데이터셋인지 설정 (멀티 데이터셋 시 구분용)
        buffer.setDataSet(index)

        // 차트가 반전되어 있을 경우 좌표를 뒤집도록 설정
        buffer.setInverted(mChart.isInverted(dataSet.axisDependency))

        // bar의 너비 설정
        buffer.setBarWidth(mChart.barData.barWidth)

        // 데이터셋에 있는 값들을 버퍼에 채움
        // 이때 실제로 막대의 좌표가 계산됨
        buffer.feed(dataSet)

        // 계산된 좌표값을 화면 픽셀 좌표로 변환
        trans.pointValuesToPixel(buffer.buffer)

        val paint = mRenderPaint
        paint.color = dataSet.color

        val arr = buffer.buffer
        var i = 0
        while (i < arr.size) {
            val left = arr[i]
            val top = arr[i + 1]
            val right = arr[i + 2]
            val bottom = arr[i + 3]

            val rect = RectF(left, top, right, bottom)

            // bar 높이보다 radius가 클 경우 깨지는 문제 방지
            val r = minOf(radius, (bottom - top) / 2f)

            // 왼쪽은 직각, 오른쪽은 둥근 모서리 적용
            val path =
                Path().apply {
                    addRoundRect(
                        rect,
                        floatArrayOf(
                            0f,
                            0f, // left-top (각지게)
                            r,
                            r, // right-top (둥글게)
                            r,
                            r, // right-bottom (둥글게)
                            0f,
                            0f, // left-bottom (각지게)
                        ),
                        Path.Direction.CW,
                    )
                }

            c.drawPath(path, paint)
            i += 4
        }
    }
}
