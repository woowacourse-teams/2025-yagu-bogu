package com.yagubogu.presentation.stats.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.yagubogu.databinding.FragmentDetailStatsBinding

@Suppress("ktlint:standard:backing-property-naming")
class StatsDetailFragment : Fragment() {
    private var _binding: FragmentDetailStatsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StatsDetailViewModel by viewModels { StatsDetailViewModelFactory() }

    private val vsTeamStatAdapter: VsTeamStatAdapter by lazy { VsTeamStatAdapter() }

    private val barChartManager: BarChartManager by lazy {
        BarChartManager(requireContext(), binding.barChart)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentDetailStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        setupBindings()
        setupObservers()
        barChartManager.setupChart()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            viewModel.fetchAll()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupBindings() {
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        binding.rvVsTeamWinningPercentage.adapter = vsTeamStatAdapter
    }

    private fun setupObservers() {
        viewModel.vsTeamStats.observe(viewLifecycleOwner) { value: List<VsTeamStatItem> ->
            vsTeamStatAdapter.submitList(value)
        }
        viewModel.stadiumVisitCounts.observe(viewLifecycleOwner) { value: List<StadiumVisitCount> ->
            barChartManager.loadData(value)
        }
    }
}
