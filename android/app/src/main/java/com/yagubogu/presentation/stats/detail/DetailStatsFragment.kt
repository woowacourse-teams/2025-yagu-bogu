package com.yagubogu.presentation.stats.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.yagubogu.databinding.FragmentDetailStatsBinding
import com.yagubogu.domain.model.Team

@Suppress("ktlint:standard:backing-property-naming")
class DetailStatsFragment : Fragment() {
    private var _binding: FragmentDetailStatsBinding? = null
    private val binding get() = _binding!!

    private val vsTeamStatAdapter: VsTeamStatAdapter by lazy { VsTeamStatAdapter() }

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
        binding.rvVsTeamWinningPercentage.adapter = vsTeamStatAdapter
        vsTeamStatAdapter.submitList(DUMMY_VS_TEAM_STATS)
    }

    companion object {
        private val DUMMY_VS_TEAM_STATS =
            listOf(
                VsTeamStat(
                    rank = 1,
                    name = "두산",
                    team = Team.OB,
                    winCounts = 4,
                    drawCounts = 0,
                    loseCounts = 0,
                    winningPercentage = 100.0,
                ),
                VsTeamStat(
                    rank = 2,
                    name = "LG",
                    team = Team.LG,
                    winCounts = 3,
                    drawCounts = 1,
                    loseCounts = 0,
                    winningPercentage = 75.0,
                ),
                VsTeamStat(
                    rank = 3,
                    name = "키움",
                    team = Team.WO,
                    winCounts = 2,
                    drawCounts = 0,
                    loseCounts = 1,
                    winningPercentage = 66.6,
                ),
                VsTeamStat(
                    rank = 4,
                    name = "KT",
                    team = Team.KT,
                    winCounts = 2,
                    drawCounts = 0,
                    loseCounts = 2,
                    winningPercentage = 50.0,
                ),
                VsTeamStat(
                    rank = 5,
                    name = "삼성",
                    team = Team.SS,
                    winCounts = 1,
                    drawCounts = 0,
                    loseCounts = 2,
                    winningPercentage = 33.3,
                ),
                VsTeamStat(
                    rank = 6,
                    name = "NC",
                    team = Team.NC,
                    winCounts = 1,
                    drawCounts = 1,
                    loseCounts = 2,
                    winningPercentage = 25.0,
                ),
                VsTeamStat(
                    rank = 7,
                    name = "롯데",
                    team = Team.LT,
                    winCounts = 1,
                    drawCounts = 0,
                    loseCounts = 4,
                    winningPercentage = 20.0,
                ),
                VsTeamStat(
                    rank = 8,
                    name = "SSG",
                    team = Team.SK,
                    winCounts = 0,
                    drawCounts = 0,
                    loseCounts = 0,
                    winningPercentage = 0.0,
                ),
                VsTeamStat(
                    rank = 9,
                    name = "한화",
                    team = Team.HH,
                    winCounts = 0,
                    drawCounts = 0,
                    loseCounts = 0,
                    winningPercentage = 0.0,
                ),
            )
    }
}
