package com.yagubogu.presentation.stats.my

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.yagubogu.R
import com.yagubogu.databinding.FragmentMyStatsBinding
import kotlin.math.roundToInt

@Suppress("ktlint:standard:backing-property-naming")
class MyStatsFragment : Fragment() {
    private var _binding: FragmentMyStatsBinding? = null
    private val binding: FragmentMyStatsBinding get() = _binding!!

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
        bindPieChartTexts()
    }

    private fun setupChart() {
        binding.pieChart.apply {
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

    private fun loadChartData() {
        val entries = ArrayList<PieEntry>()
        entries.add(PieEntry(winRate.toFloat(), "Win"))
        entries.add(PieEntry(etcRate.toFloat(), "Etc"))

        val myStatsChartDataSet = PieDataSet(entries, MY_ATTENDANCE_WIN_RATE_DESCRIPTION)

        myStatsChartDataSet.colors =
            listOf(
                requireContext().getColor(R.color.primary500),
                requireContext().getColor(R.color.gray300),
            )
        val pieData = PieData(myStatsChartDataSet)
        pieData.setDrawValues(false)

        binding.pieChart.data = pieData
        binding.pieChart.invalidate()
    }

    private fun bindPieChartTexts() {
        binding.tvWinCount.text = DUMMY_PIE_CHART_WIN_COUNT.toString()
        binding.tvDrawCount.text = DUMMY_PIE_CHART_DRAW_COUNT.toString()
        binding.tvLoseCount.text = DUMMY_PIE_CHART_LOSE_COUNT.toString()
        binding.tvWinPercentage.text = getString(R.string.stats_pie_chart_winning_percentage, winRate)
        binding.tvTotalAttendanceCount.text = getString(R.string.stats_my_pie_chart_attendance_count, DUMMY_PIE_CHART_TOTAL_COUNT)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val MY_ATTENDANCE_WIN_RATE_DESCRIPTION = "내 직관 승률"
        private const val DUMMY_PIE_CHART_WIN_COUNT = 190
        private const val DUMMY_PIE_CHART_DRAW_COUNT = 10
        private const val DUMMY_PIE_CHART_LOSE_COUNT = 99
        private const val DUMMY_PIE_CHART_TOTAL_COUNT =
            DUMMY_PIE_CHART_WIN_COUNT + DUMMY_PIE_CHART_DRAW_COUNT + DUMMY_PIE_CHART_LOSE_COUNT
        private val winRate: Int =
            ((DUMMY_PIE_CHART_WIN_COUNT.toFloat() / DUMMY_PIE_CHART_TOTAL_COUNT) * 100)
                .roundToInt()
        private val etcRate: Int = 100 - winRate

        private const val PIE_CHART_INSIDE_HOLE_RADIUS = 75f
        private const val PIE_CHART_ANIMATION_MILLISECOND = 1000
    }
}
