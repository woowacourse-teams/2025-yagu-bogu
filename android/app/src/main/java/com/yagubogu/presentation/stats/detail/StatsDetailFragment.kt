package com.yagubogu.presentation.stats.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.yagubogu.YaguBoguApplication
import com.yagubogu.databinding.FragmentStatsDetailBinding
import com.yagubogu.presentation.util.ScrollToTop

@Suppress("ktlint:standard:backing-property-naming")
class StatsDetailFragment :
    Fragment(),
    ScrollToTop {
    private var _binding: FragmentStatsDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StatsDetailViewModel by viewModels {
        val app = requireActivity().application as YaguBoguApplication
        StatsDetailViewModelFactory(
            app.statsRepository,
            app.checkInsRepository,
        )
    }

    private val vsTeamStatAdapter: VsTeamStatAdapter by lazy { VsTeamStatAdapter() }
    private val barChartManager: BarChartManager by lazy {
        BarChartManager(requireContext(), binding.barChart)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentStatsDetailBinding.inflate(inflater, container, false)
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

    override fun scrollToTop() {
        binding.nsvRoot.smoothScrollTo(0, 0)
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
