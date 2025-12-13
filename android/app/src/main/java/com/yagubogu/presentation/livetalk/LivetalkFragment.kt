package com.yagubogu.presentation.livetalk

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.yagubogu.presentation.util.ScrollToTop
import com.yagubogu.ui.livetalk.LivetalkScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LivetalkFragment :
    Fragment(),
    ScrollToTop {
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
    ): View =
        ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                LivetalkScreen(viewModel)
            }
        }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            viewModel.fetchGames()
        }
    }

    override fun scrollToTop() {
        viewModel.scrollToTop()
    }

    private fun setupObservers() {
//        viewModel.livetalkStadiumItems.observe(viewLifecycleOwner) { value: List<LivetalkStadiumItem> ->
//            val visibility = if (value.isEmpty()) View.VISIBLE else View.GONE
//            binding.ivEmptyGame.visibility = visibility
//            binding.tvEmptyGame.visibility = visibility
//        }
    }
}
