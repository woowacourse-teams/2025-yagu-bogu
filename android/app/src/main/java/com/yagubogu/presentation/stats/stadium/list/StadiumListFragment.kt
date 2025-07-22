package com.yagubogu.presentation.stats.stadium.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.yagubogu.databinding.FragmentStadiumListBinding

@Suppress("ktlint:standard:backing-property-naming")
class StadiumListFragment : Fragment() {
    private var _binding: FragmentStadiumListBinding? = null
    private val binding: FragmentStadiumListBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentStadiumListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupRecyclerView() {
        val adapter = StadiumListAdapter()
        binding.rvStadiumList.adapter = adapter
        adapter.submitList(DUMMY_STADIUMS)

        binding.rvStadiumList.addItemDecoration(StadiumListItemDecoration(context = requireContext()))
    }

    companion object {
        private val DUMMY_STADIUMS =
            listOf(
                StadiumUiModel(1, "\uD83D\uDC2F", "광주 KIA 챔피언스필드"),
                StadiumUiModel(2, "\uD83C\uDFDF\uFE0F", "잠실 야구장"),
                StadiumUiModel(3, "\uD83E\uDDB8", "고척 스카이돔"),
                StadiumUiModel(4, "\uD83E\uDDD9", "수원 KT 위즈파크"),
                StadiumUiModel(5, "\uD83E\uDD81", "대구 삼성 라이온즈파크"),
                StadiumUiModel(6, "\uD83C\uDF3A", "부산 사직야구장"),
                StadiumUiModel(7, "\uD83D\uDE80", "인천 SSG 랜더스필드"),
                StadiumUiModel(8, "\uD83E\uDD85", "대전 한화생명 볼파크"),
                StadiumUiModel(9, "\uD83E\uDD95", "창원 NC 파크"),
            )
    }
}
