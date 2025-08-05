package com.yagubogu.presentation.livetalk

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.yagubogu.databinding.FragmentLiveTalkBinding
import com.yagubogu.domain.model.Team
import com.yagubogu.presentation.livetalk.stadium.LivetalkStadiumAdapter
import com.yagubogu.presentation.livetalk.stadium.LivetalkStadiumItem

@Suppress("ktlint:standard:backing-property-naming")
class LivetalkFragment : Fragment() {
    private var _binding: FragmentLiveTalkBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentLiveTalkBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        setupBindings()
    }

    private fun setupBindings() {
        val livetalkStadiumAdapter = LivetalkStadiumAdapter()
        val linearLayoutManager = LinearLayoutManager(requireContext())
        binding.rvLiveTalkStadium.apply {
            adapter = livetalkStadiumAdapter
            layoutManager = linearLayoutManager
        }
        livetalkStadiumAdapter.submitList(DUMMY_LIVETALK_STADIUM_ITEMS)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private val DUMMY_LIVETALK_STADIUM_ITEMS: List<LivetalkStadiumItem> =
            listOf(
                LivetalkStadiumItem(
                    stadiumName = "고척 스카이돔",
                    userCount = 120,
                    awayTeam = Team.DOOSAN,
                    homeTeam = Team.KIWOOM,
                    isVerified = true,
                ),
                LivetalkStadiumItem(
                    stadiumName = "잠실 야구장",
                    userCount = 58,
                    awayTeam = Team.LOTTE,
                    homeTeam = Team.LG,
                    isVerified = false,
                ),
                LivetalkStadiumItem(
                    stadiumName = "인천 SSG 랜더스필드",
                    userCount = 44,
                    awayTeam = Team.KIA,
                    homeTeam = Team.SSG,
                    isVerified = false,
                ),
                LivetalkStadiumItem(
                    stadiumName = "대전 한화생명 볼파크",
                    userCount = 26,
                    awayTeam = Team.SAMSUNG,
                    homeTeam = Team.HANWHA,
                    isVerified = false,
                ),
                LivetalkStadiumItem(
                    stadiumName = "창원 NC 파크",
                    userCount = 10,
                    awayTeam = Team.KT,
                    homeTeam = Team.NC,
                    isVerified = false,
                ),
            )
    }
}
