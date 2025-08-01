package com.yagubogu.presentation.stats.my

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.yagubogu.R
import com.yagubogu.YaguBoguApplication
import com.yagubogu.databinding.FragmentMyStatsBinding

@Suppress("ktlint:standard:backing-property-naming")
class MyStatsFragment : Fragment() {
    private var _binding: FragmentMyStatsBinding? = null
    private val binding: FragmentMyStatsBinding get() = _binding!!

    private val viewModel: MyStatsViewModel by viewModels {
        val app = requireActivity().application as YaguBoguApplication
        MyStatsViewModelFactory(app.statsRepository, app.memberRepository)
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
        setupObservers()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupObservers() {
        viewModel.myStatsUiModel.observe(viewLifecycleOwner) { value: MyStatsUiModel ->
            binding.myStatsUiModel = value
            loadChartData(value)
        }
    }

    private fun setupChart() {
        binding.pieChart.apply {
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

    private fun loadChartData(myStatsUiModel: MyStatsUiModel) {
        val pieEntries = ArrayList<PieEntry>()
        pieEntries.add(PieEntry(myStatsUiModel.winningPercentage.toFloat(), "Win"))
        pieEntries.add(PieEntry(myStatsUiModel.etcPercentage.toFloat(), "Etc"))

        val myStatsChartDataSet = PieDataSet(pieEntries, WINNING_PERCENTAGE)

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

    companion object {
        private const val WINNING_PERCENTAGE = "내 직관 승률"

        private const val PIE_CHART_INSIDE_HOLE_RADIUS = 75f
        private const val PIE_CHART_ANIMATION_MILLISECOND = 1000
    }
}
