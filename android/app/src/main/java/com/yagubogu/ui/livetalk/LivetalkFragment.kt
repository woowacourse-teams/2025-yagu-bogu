package com.yagubogu.ui.livetalk

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.yagubogu.presentation.util.ScrollToTop
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LivetalkFragment :
    Fragment(),
    ScrollToTop {
    private val viewModel: LivetalkViewModel by viewModels()

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
}
