package com.yagubogu.presentation.stats.stadium

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.yagubogu.R
import com.yagubogu.databinding.FragmentStadiumStatsBinding

@Suppress("ktlint:standard:backing-property-naming")
class StadiumStatsFragment : Fragment() {
    private var _binding: FragmentStadiumStatsBinding? = null
    private val binding: FragmentStadiumStatsBinding get() = _binding!!

    private val dummyStadiumUiModel: StadiumStatsUiModel =
        StadiumStatsUiModel(
            "고척돔",
            listOf(
                TeamOccupancyStatus("한화", R.color.team_hanwha, 70),
                TeamOccupancyStatus("삼성", R.color.team_samsung, 30),
                TeamOccupancyStatus("기타", R.color.gray400, 10),
            ),
        )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentStadiumStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        setupChart()
        loadChartData()
        binding.stadiumStatsUiModel = dummyStadiumUiModel
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
        val pieEntries = ArrayList<PieEntry>()

        dummyStadiumUiModel.teamOccupancyStatuses.forEach { teamOccupancyStatus: TeamOccupancyStatus ->
            pieEntries.add(PieEntry(teamOccupancyStatus.percentage.toFloat(), teamOccupancyStatus.name))
        }

        val stadiumStatsChartDataSet = PieDataSet(pieEntries, STADIUM_CHART_DESCRIPTION)

        stadiumStatsChartDataSet.colors = dummyStadiumUiModel.teamOccupancyStatuses.map { requireContext().getColor(it.teamColor) }
        val pieData = PieData(stadiumStatsChartDataSet)
        pieData.setDrawValues(false)

        binding.pieChart.data = pieData
        binding.pieChart.invalidate()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val STADIUM_CHART_DESCRIPTION = "오늘의 구장 현황"

        private const val PIE_CHART_INSIDE_HOLE_RADIUS = 75f
        private const val PIE_CHART_ANIMATION_MILLISECOND = 1000
    }
}
