package com.yagubogu.presentation.livetalk

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.yagubogu.databinding.FragmentLivetalkBinding
import com.yagubogu.presentation.livetalk.chat.LivetalkChatActivity
import com.yagubogu.presentation.livetalk.stadium.LivetalkStadiumItem
import com.yagubogu.presentation.util.ScrollToTop
import dagger.hilt.android.AndroidEntryPoint

@Suppress("ktlint:standard:backing-property-naming")
@AndroidEntryPoint
class LivetalkFragment :
    Fragment(),
    ScrollToTop {
    private var _binding: FragmentLivetalkBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LivetalkViewModel by viewModels()

//    private val livetalkStadiumAdapter by lazy {
//        LivetalkStadiumAdapter(
//            object : LivetalkStadiumViewHolder.Handler {
//                override fun onItemClick(item: LivetalkStadiumItem) {
//                    val intent =
//                        LivetalkChatActivity.newIntent(
//                            requireContext(),
//                            item.gameId,
//                            item.isVerified,
//                        )
//                    startActivity(intent)
//                }
//            },
//        )
//    }

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
        setupObservers()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            viewModel.fetchGames()
        }
    }

    override fun scrollToTop() {
        binding.rvLivetalkStadium.smoothScrollToPosition(0)
    }

    private fun setupObservers() {
//        viewModel.livetalkStadiumItems.observe(viewLifecycleOwner) { value: List<LivetalkStadiumItem> ->
//            val visibility = if (value.isEmpty()) View.VISIBLE else View.GONE
//            binding.ivEmptyGame.visibility = visibility
//            binding.tvEmptyGame.visibility = visibility
//        }
    }
}
