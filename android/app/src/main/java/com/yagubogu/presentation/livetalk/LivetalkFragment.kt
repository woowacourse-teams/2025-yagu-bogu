package com.yagubogu.presentation.livetalk

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.yagubogu.databinding.FragmentLivetalkBinding
import com.yagubogu.domain.model.Team
import com.yagubogu.presentation.livetalk.stadium.LivetalkStadiumAdapter
import com.yagubogu.presentation.livetalk.stadium.LivetalkStadiumItem
import com.yagubogu.presentation.livetalk.stadium.LivetalkStadiumViewHolder

@Suppress("ktlint:standard:backing-property-naming")
class LivetalkFragment : Fragment() {
    private var _binding: FragmentLivetalkBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentLivetalkBinding.inflate(inflater, container, false)
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
        val livetalkStadiumAdapter =
            LivetalkStadiumAdapter(
                object : LivetalkStadiumViewHolder.Handler {
                    override fun onItemClick(item: LivetalkStadiumItem) {
                        // Todo: 구장별 채팅 연동 필요
                        val intent = Intent(binding.root.context, LivetalkChatActivity::class.java)
                        binding.root.context.startActivity(intent)
                    }
                },
            )
        val linearLayoutManager = LinearLayoutManager(requireContext())
        binding.rvLivetalkStadium.apply {
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
                    awayTeam = Team.OB,
                    homeTeam = Team.WO,
                    isVerified = true,
                ),
                LivetalkStadiumItem(
                    stadiumName = "잠실 야구장",
                    userCount = 58,
                    awayTeam = Team.LT,
                    homeTeam = Team.LG,
                    isVerified = false,
                ),
                LivetalkStadiumItem(
                    stadiumName = "인천 SSG 랜더스필드",
                    userCount = 44,
                    awayTeam = Team.HT,
                    homeTeam = Team.SK,
                    isVerified = false,
                ),
                LivetalkStadiumItem(
                    stadiumName = "대전 한화생명 볼파크",
                    userCount = 26,
                    awayTeam = Team.SS,
                    homeTeam = Team.HH,
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
