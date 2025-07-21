package com.yagubogu.presentation.stats.my

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.TextPaint
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.TypefaceSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.yagubogu.R
import com.yagubogu.databinding.FragmentMyStatsBinding

@Suppress("ktlint:standard:backing-property-naming")
class MyStatsFragment : Fragment() {
    private var _binding: FragmentMyStatsBinding? = null
    private val binding: FragmentMyStatsBinding get() = _binding!!
    private val pieChart: PieChart
        get() = binding.pieChart

    private val pretendardBold by lazy {
        ResourcesCompat.getFont(requireContext(), R.font.pretendard_bold)
    }
    private val pretendardRegular by lazy {
        ResourcesCompat.getFont(requireContext(), R.font.pretendard_medium)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentMyStatsBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        setupChart()
        loadChartData()
    }

    private fun setupChart() {
        pieChart.apply {
            legend.isEnabled = false

            isDrawHoleEnabled = true
            setHoleColor(Color.TRANSPARENT)
            holeRadius = PIE_CHART_INSIDE_HOLE_RADIUS

            description.isEnabled = false
            setDrawEntryLabels(false)
            setDrawCenterText(true)
            centerText = createCenterText()
            setCenterTextSize(PIE_CHART_INSIDE_TEXT_SIZE)

            isRotationEnabled = false
            setTouchEnabled(false)
            animateY(PIE_CHART_ANIMATION_MILLISECOND)
        }
    }

    private fun createCenterText(): SpannableString {
        val centerText = SpannableString("75%\n24 경기")

        centerText.apply {
            setSpan(RelativeSizeSpan(PIE_CHART_INSIDE_TEXT_FIRST_LINE_WEIGHT), 0, 3, 0)
            setSpan(
                ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.primary500)),
                0,
                3,
                0,
            )
            pretendardBold?.let { setSpan(CustomTypefaceSpan(it), 0, 3, 0) }

            setSpan(
                RelativeSizeSpan(PIE_CHART_INSIDE_TEXT_SECOND_LINE_WEIGHT),
                4,
                centerText.length,
                0,
            )
            setSpan(
                ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.gray500)),
                4,
                centerText.length,
                0,
            )
            pretendardRegular?.let { setSpan(CustomTypefaceSpan(it), 4, centerText.length, 0) }
        }

        return centerText
    }

    private fun loadChartData() {
        val entries = ArrayList<PieEntry>()
        entries.add(PieEntry(75f, "Win"))
        entries.add(PieEntry(25f, "Etc"))

        val dataSet = PieDataSet(entries, "내 직관 승률")

        dataSet.colors =
            listOf(
                ContextCompat.getColor(requireContext(), R.color.primary500),
                ContextCompat.getColor(requireContext(), R.color.gray300),
            )
        val data = PieData(dataSet)
        data.setDrawValues(false)

        pieChart.data = data
        pieChart.invalidate()
    }

    inner class CustomTypefaceSpan(
        private val typeface: Typeface,
    ) : TypefaceSpan("") {
        override fun updateDrawState(ds: TextPaint) {
            ds.typeface = typeface
        }

        override fun updateMeasureState(paint: TextPaint) {
            paint.typeface = typeface
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val PIE_CHART_INSIDE_TEXT_SIZE = 14f
        private const val PIE_CHART_INSIDE_TEXT_FIRST_LINE_WEIGHT = 2.8f
        private const val PIE_CHART_INSIDE_TEXT_SECOND_LINE_WEIGHT = 1.2f
        private const val PIE_CHART_INSIDE_HOLE_RADIUS = 75f
        private const val PIE_CHART_ANIMATION_MILLISECOND = 1000
    }
}
